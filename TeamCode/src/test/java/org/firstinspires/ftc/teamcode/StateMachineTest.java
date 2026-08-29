package org.firstinspires.ftc.teamcode;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.firstinspires.ftc.teamcode.lib.fsm.State;
import org.firstinspires.ftc.teamcode.lib.fsm.StateMachine;
import org.firstinspires.ftc.teamcode.lib.fsm.transition.Transition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Requires (Gradle, TeamCode/build.gradle):
 * testImplementation 'junit:junit:4.13.2'
 * testImplementation 'pl.pragmatists:JUnitParams:1.1.1'
 * testImplementation 'org.mockito:mockito-core:5.7.0'
 *
 * These tests exercise {@link StateMachine} against the {@link State}/{@link Transition}
 * interfaces directly, via a small local {@link #transition} helper — not against
 * {@code TransitionBase} — so they stay valid regardless of how that class is implemented.
 */
@RunWith(JUnitParamsRunner.class)
public class StateMachineTest {

    private enum TestState implements State { A, B, C, D }

    @Before
    public void resetSingleton() {
        StateMachine.resetInstance();
    }

    private static Transition transition(State from, State to, BooleanSupplier condition, Runnable action) {
        return new Transition() {
            @Override public State from() { return from; }
            @Override public State to() { return to; }
            @Override public boolean condition() { return condition.getAsBoolean(); }
            @Override public Runnable action() { return action; }
        };
    }

    private static Transition transition(State from, State to, BooleanSupplier condition) {
        return transition(from, to, condition, null);
    }

    private static Transition anyStateTransition(State to, BooleanSupplier condition, Runnable action) {
        return transition(null, to, condition, action);
    }

    private static Transition anyStateTransition(State to, BooleanSupplier condition) {
        return anyStateTransition(to, condition, null);
    }

    // ---------- build() validation ----------

    @Test
    public void build_throwsWhenInitialNeverSet() {
        StateMachine.Builder builder = StateMachine.builder();
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    public void build_succeedsWithInitialSet() {
        StateMachine sm = StateMachine.builder()
                .initial(TestState.A)
                .build();

        assertNotNull(sm);
    }

    // ---------- initial state ----------

    @Test
    @Parameters({"A", "B", "C", "D"})
    public void initialize_setsCurrentStateAndFiresInitialStateChange(String initialName) {
        TestState initial = TestState.valueOf(initialName);
        List<Object[]> events = new ArrayList<>();

        StateMachine sm = StateMachine.builder()
                .initial(initial)
                .onStateChange((from, to) -> events.add(new Object[]{from, to}))
                .build();

        sm.initialize();

        assertEquals(initial, sm.getCurrentState());
        assertEquals(1, events.size());
        assertNull(events.get(0)[0]);
        assertEquals(initial, events.get(0)[1]);
    }

    // ---------- basic transition firing ----------

    @Test
    @Parameters({"true", "false"})
    public void execute_transitionsOnlyWhenConditionTrue(boolean conditionMet) {
        StateMachine sm = StateMachine.builder()
                .initial(TestState.A)
                .transition(transition(TestState.A, TestState.B, () -> conditionMet))
                .build();

        sm.initialize();
        sm.execute();

        assertEquals(conditionMet ? TestState.B : TestState.A, sm.getCurrentState());
    }

    @Test
    public void execute_runsTransitionActionSynchronouslyWhenItFires() {
        Runnable action = mock(Runnable.class);

        StateMachine sm = StateMachine.builder()
                .initial(TestState.A)
                .transition(transition(TestState.A, TestState.B, () -> true, action))
                .build();

        sm.initialize();
        sm.execute();

        assertEquals(TestState.B, sm.getCurrentState());
        verify(action, times(1)).run();
    }

    @Test
    public void execute_worksWithNoActionAttached() {
        StateMachine sm = StateMachine.builder()
                .initial(TestState.A)
                .transition(transition(TestState.A, TestState.B, () -> true))
                .build();

        sm.initialize();
        sm.execute(); // must not throw with a null action

        assertEquals(TestState.B, sm.getCurrentState());
    }

    // ---------- only one transition fires per execute() ----------

    @Test
    public void execute_firesAtMostOneTransitionPerCall() {
        StateMachine sm = StateMachine.builder()
                .initial(TestState.A)
                .transition(transition(TestState.A, TestState.B, () -> true))
                .transition(transition(TestState.B, TestState.C, () -> true))
                .build();

        sm.initialize();
        sm.execute();
        assertEquals(TestState.B, sm.getCurrentState());

        sm.execute();
        assertEquals(TestState.C, sm.getCurrentState());
    }

    // ---------- transition selection ordering ----------

    @Test
    @Parameters(method = "transitionOrderCases")
    public void execute_firstRegisteredMatchingTransitionWinsOnTie(TestState first, TestState second, TestState expected) {
        StateMachine sm = StateMachine.builder()
                .initial(TestState.A)
                .transition(transition(TestState.A, first, () -> true))
                .transition(transition(TestState.A, second, () -> true))
                .build();

        sm.initialize();
        sm.execute();

        assertEquals(expected, sm.getCurrentState());
    }

    private Object[] transitionOrderCases() {
        return new Object[]{
                new Object[]{TestState.B, TestState.C, TestState.B},
                new Object[]{TestState.C, TestState.B, TestState.C}
        };
    }

    @Test
    @Parameters({"true", "false"})
    public void execute_globalTransitionTakesPriorityOverLocal(boolean globalConditionMet) {
        StateMachine sm = StateMachine.builder()
                .initial(TestState.A)
                .transition(transition(TestState.A, TestState.B, () -> true))
                .transition(anyStateTransition(TestState.C, () -> globalConditionMet))
                .build();

        sm.initialize();
        sm.execute();

        assertEquals(globalConditionMet ? TestState.C : TestState.B, sm.getCurrentState());
    }

    @Test
    public void anyStateTransition_hasNullFromAndReportsGlobal() {
        Transition t = anyStateTransition(TestState.A, () -> true);

        assertNull(t.from());
        assertTrue(t.isGlobal());
    }

    @Test
    public void ordinaryTransition_isNotGlobal() {
        Transition t = transition(TestState.A, TestState.B, () -> true);

        assertFalse(t.isGlobal());
    }

    // ---------- onStateChange listener ----------

    @Test
    public void execute_stateChangeListenerReceivesPreviousAndNewState() {
        List<Object[]> events = new ArrayList<>();

        StateMachine sm = StateMachine.builder()
                .initial(TestState.A)
                .transition(transition(TestState.A, TestState.B, () -> true))
                .onStateChange((from, to) -> events.add(new Object[]{from, to}))
                .build();

        sm.initialize(); // fires (null, A)
        sm.execute();     // fires (A, B)

        assertEquals(2, events.size());
        assertEquals(TestState.A, events.get(1)[0]);
        assertEquals(TestState.B, events.get(1)[1]);
    }

    // ---------- singleton behavior ----------

    @Test
    public void getInstance_returnsSameInstanceUntilReset() {
        StateMachine first = StateMachine.getInstance();
        StateMachine second = StateMachine.getInstance();
        assertSame(first, second);

        StateMachine.resetInstance();
        StateMachine third = StateMachine.getInstance();
        assertNotSame(first, third);
    }

    @Test
    @Parameters({"1", "2", "3"})
    public void builder_alwaysConfiguresCurrentSingleton(int iteration) {
        StateMachine.resetInstance();
        StateMachine viaGetInstance = StateMachine.getInstance();
        StateMachine viaBuilder = StateMachine.builder()
                .initial(TestState.A)
                .build();

        assertSame(viaGetInstance, viaBuilder);
    }
}