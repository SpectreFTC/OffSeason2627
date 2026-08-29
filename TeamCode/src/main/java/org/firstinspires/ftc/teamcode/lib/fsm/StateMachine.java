package org.firstinspires.ftc.teamcode.lib.fsm;

import org.firstinspires.ftc.teamcode.lib.fsm.transition.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A hybrid Mealy/Moore finite state machine. Depends only on the {@link State} and
 * {@link Transition} interfaces — never on any particular {@link Transition}
 * implementation such as {@code TransitionBase}.
 *
 * <p>Two independent kinds of behavior:
 * <ul>
 *   <li>A transition's {@link Transition#action()} runs once, synchronously, the instant
 *       that transition fires — an edge action.
 *   <li>A state's start/stop actions, registered via {@link Builder#whileIn}, run once
 *       each: {@code start} the moment the machine enters that state, {@code stop} the
 *       moment it leaves — regardless of which transition caused either. This is the
 *       right shape for anything that loops on its own once started (e.g. an FTCLib
 *       {@code Command} handed to a {@code CommandScheduler}) — you start it once and
 *       stop it once, you don't re-invoke it every tick yourself.
 * </ul>
 *
 * <p>Firing order when a transition fires: the old state's {@code stop} action runs
 * first, then the transition's own {@link Transition#action()}, then the new state's
 * {@code start} action. The initial state's {@code start} action runs during
 * {@link #initialize()}, since entering the initial state is still "entering a state."
 *
 * <p>Only one transition fires per {@link #execute()} call, checking global transitions
 * before ones local to the current state, in registration order.
 *
 * <p>Configuration is done through {@link #builder()}, which always configures the
 * singleton returned by {@link #getInstance()}. Call {@link #resetInstance()} before
 * rebuilding in a new session (e.g. at the top of an FTC opmode's {@code init()}).
 *
 * <p>When {@code start}/{@code stop} wrap an FTCLib {@code Command}, always create a
 * fresh instance on {@code start} and cancel that exact instance on {@code stop} —
 * never schedule/cancel one shared, reused command object, which is a common source of
 * scheduler bookkeeping errors:
 * <pre>{@code
 * StateMachine.resetInstance();
 *
 * // holds a reference to whichever instance is currently running, so stop()
 * // cancels the exact one that start() most recently scheduled
 * Command[] activeFlywheelCommand = new Command[1];
 *
 * StateMachine sm = StateMachine.builder()
 *     .initial(RobotState.IDLE)
 *     .whileIn(RobotState.AIMED,
 *         () -> {
 *             activeFlywheelCommand[0] = new HoldFlywheelCommand(shooter);
 *             scheduler.schedule(activeFlywheelCommand[0]);
 *         },
 *         () -> {
 *             if (activeFlywheelCommand[0] != null) {
 *                 scheduler.cancel(activeFlywheelCommand[0]);
 *                 activeFlywheelCommand[0] = null;
 *             }
 *         })
 *     .transition(new TransitionBase(RobotState.IDLE, RobotState.AIMED, () -> gamepad1.a))
 *     .transition(TransitionBase.anyState(RobotState.IDLE, () -> gamepad1.back))
 *     .build();
 *
 * sm.initialize();
 * // each loop:
 * sm.execute();
 * }</pre>
 */
public class StateMachine {
    private static StateMachine instance;

    private final List<Transition> transitions = new ArrayList<>();
    private final Map<State, Runnable> enterActions = new HashMap<>();
    private final Map<State, Runnable> exitActions = new HashMap<>();

    private State initialState;
    private State currentState;
    private BiConsumer<State, State> stateChangeListener;

    private StateMachine() {}

    /**
     * Returns the shared {@code StateMachine} instance, creating it on first call.
     */
    public static StateMachine getInstance() {
        if (instance == null) {
            instance = new StateMachine();
        }
        return instance;
    }

    /**
     * Discards the current singleton so the next {@link #getInstance()} or
     * {@link #builder()} call starts from a clean slate.
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * Starts a {@link Builder} bound to the current singleton (see {@link #getInstance()}).
     */
    public static Builder builder() {
        return new Builder(getInstance());
    }

    /**
     * Fluent configuration surface for {@link StateMachine}. All methods mutate the
     * bound singleton and return {@code this}.
     */
    public static class Builder {
        private final StateMachine sm;

        private Builder(StateMachine sm) {
            this.sm = sm;
        }

        /** Sets which state the machine starts in when {@link #initialize()} is called. */
        public Builder initial(State state) {
            sm.initialState = state;
            return this;
        }

        /** Registers a transition. Order matters for same-state ties — first registered wins. */
        public Builder transition(Transition transition) {
            sm.transitions.add(transition);
            return this;
        }

        /**
         * Registers {@code start}/{@code stop} for {@code state}: {@code start} runs once
         * when the machine enters the state, {@code stop} runs once when it leaves —
         * regardless of which transition caused either. Calling this again for the same
         * state replaces its previous start/stop pair.
         */
        public Builder whileIn(State state, Runnable start, Runnable stop) {
            sm.enterActions.put(state, start);
            if (stop != null) {
                sm.exitActions.put(state, stop);
            }
            return this;
        }

        /** {@link #whileIn(State, Runnable, Runnable)} with no stop action. */
        public Builder whileIn(State state, Runnable start) {
            return whileIn(state, start, null);
        }

        /**
         * Registers a listener invoked on every transition with {@code (previousState, newState)}.
         * {@code previousState} is {@code null} for the very first entry into the initial state.
         */
        public Builder onStateChange(BiConsumer<State, State> listener) {
            sm.stateChangeListener = listener;
            return this;
        }

        /**
         * Validates the configuration and returns the configured {@link StateMachine}.
         *
         * @throws IllegalStateException if {@link #initial} was never called
         */
        public StateMachine build() {
            if (sm.initialState == null) {
                throw new IllegalStateException("StateMachine: no initial state set");
            }
            return sm;
        }
    }

    /**
     * Enters the configured initial state, notifies the state-change listener with
     * {@code (null, initialState)}, then runs the initial state's start action, if any.
     */
    public void initialize() {
        currentState = initialState;
        notifyStateChange(null, currentState);
        runEnterAction(currentState);
    }

    /**
     * Checks for a matching transition and fires at most one per call.
     */
    public void execute() {
        Transition match = findMatch();
        if (match != null) {
            fire(match);
        }
    }

    private Transition findMatch() {
        for (Transition t : transitions) {
            if (t.isGlobal() && t.condition()) return t;
        }
        for (Transition t : transitions) {
            if (!t.isGlobal() && currentState.equals(t.from()) && t.condition()) return t;
        }
        return null;
    }

    private void fire(Transition t) {
        runExitAction(currentState);
        if (t.action() != null) {
            t.action().run();
        }
        State previous = currentState;
        currentState = t.to();
        notifyStateChange(previous, currentState);
        runEnterAction(currentState);
    }

    private void runEnterAction(State state) {
        Runnable action = enterActions.get(state);
        if (action != null) {
            action.run();
        }
    }

    private void runExitAction(State state) {
        Runnable action = exitActions.get(state);
        if (action != null) {
            action.run();
        }
    }

    private void notifyStateChange(State from, State to) {
        if (stateChangeListener != null) {
            stateChangeListener.accept(from, to);
        }
    }

    /**
     * @return the currently active state
     */
    public State getCurrentState() {
        return currentState;
    }
}