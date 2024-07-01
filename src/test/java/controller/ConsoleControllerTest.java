package controller;

import cms.util.maybe.NoMaybeValue;
import controller.ControllerFactory;
import controller.DeterministicHexInformation.CritterHex;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import model.ReadOnlyWorld;
import org.junit.jupiter.api.Test;

import static controller.DeterministicHexInformation.NonCritterHex.EMPTY_HEX;
import static controller.DeterministicHexInformation.NonCritterHex.ROCK_HEX;
import static controller.DeterministicHexInformation.foodHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ConsoleControllerTest {
    private static final String COMMON_TEST_PATH = Paths.get("src", "test", "resources", "A5files").toString();


    public void testRandomWorld() {
        // Test a randomly generated world only contains empty and rock.
        final var controller = ControllerFactory.getConsoleController();
        controller.newWorld();
        final var world = controller.getReadOnlyWorld();
        for (int i = -1; i < 100; i++) {
            for (int j = -1; j < 100; j++) {
                if ((i + j) % 2 != 0) continue;

                final var hexValue = world.getTerrainInfo(i, j);
                if (i == -1 || j == -1) {
                    assertEquals(-1, hexValue, "Out of bound tiles must have rock's hex values");
                }
                assertTrue(hexValue == 0 || hexValue == -1,
                        "A randomly initialized world should only contain empty and rock tiles!");
            }
        }
    }

    @Test
    public void emptyWorldTest() {
        // An empty 10x10 empty world.
        // This test mostly tests that the student's solution return -1 for hex outside of the world.
        runTest(
                Paths.get(COMMON_TEST_PATH, "empty.wld").toString(),
                // Step 0
                new StepState(0, new ExpectedHex(3, 3, EMPTY_HEX))
        );
    }

    @Test
    public void smallWorldTest() {
        // A simple 1x1 empty world.
        // This test mostly tests that the student's solution return -1 for hex outside of the world.
        runTest(
                Paths.get(COMMON_TEST_PATH, "small_world.txt").toString(),
                // Step 0: the state of the world after load
                new StepState(
                        0,
                        new ExpectedHex(0, 0, EMPTY_HEX),
                        new ExpectedHex(1, 1, ROCK_HEX)),
                // Step 1: the state of the world after one step
                new StepState(
                        0,
                        new ExpectedHex(0, 0, EMPTY_HEX),
                        new ExpectedHex(1, 1, ROCK_HEX),
                        new ExpectedHex(-1, 31, ROCK_HEX),
                        new ExpectedHex(-1, -1, ROCK_HEX))
        );
    }

    @Test
    public void spaceWorldTest() {
        // A simple 1x1 world where the critter does nothing.
        runTest(
                Paths.get(COMMON_TEST_PATH, "space_world.txt").toString(),
                // Step 0
                new StepState(
                        1,
                        new ExpectedHex(0, 0, CritterHex.builderWithEnergy(500).build())
                ),
                // Step 1
                new StepState(
                        1,
                        new ExpectedHex(0, 0, CritterHex.builderWithEnergy(500).build())
                )
        );
    }

    private void runTest(String worldFile, StepState initialState, StepState... steps) {
        final var controller = ControllerFactory.getConsoleController();
        assertTrue(controller.loadWorld(worldFile, false, false), String.format("World file %s failed to load.", worldFile));
        checkState(controller.getReadOnlyWorld(), 0, initialState);
        int stepId = 0;
        while (stepId < steps.length) {
            final var stepState = steps[stepId];
            controller.advanceTime(1);
            stepId++;
            checkState(controller.getReadOnlyWorld(), stepId, stepState);
        }
    }

    private static void checkState(ReadOnlyWorld world, int stepId, StepState stepState) {
        assertEquals(stepId, world.getSteps(), "Step counter disagrees in step " + stepId);
        assertEquals(stepState.population, world.getNumberOfAliveCritters(), "Critter population disagrees in step " + stepId);
        for (final var expectedHex : stepState.expectedHexList) {
            final var actualHexInformation =
                    DeterministicHexInformation.fromWorldLocation(world, expectedHex.column, expectedHex.row);
            assertEquals(
                    expectedHex.information,
                    actualHexInformation,
                    "Hex information disagrees at (" + expectedHex.column + ", " + expectedHex.row + ") in step " + stepId);
        }
    }

    private static final class StepState {
        public final int population;
        public final List<ExpectedHex> expectedHexList;

        public StepState(int population, ExpectedHex... expectedHexList) {
            this.population = population;
            this.expectedHexList = Arrays.asList(expectedHexList);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StepState stepState = (StepState) o;
            return population == stepState.population &&
                    expectedHexList.equals(stepState.expectedHexList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(population, expectedHexList);
        }

        @Override
        public String toString() {
            return String.format("StepState{population=%d, expectedHexList=%s}", population, expectedHexList);
        }
    }

    private static final class ExpectedHex {
        public final int column;
        public final int row;
        public final DeterministicHexInformation information;

        public ExpectedHex(int column, int row, DeterministicHexInformation information) {
            this.column = column;
            this.row = row;
            this.information = information;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExpectedHex that = (ExpectedHex) o;
            return column == that.column &&
                    row == that.row &&
                    information.equals(that.information);
        }

        @Override
        public int hashCode() {
            return Objects.hash(column, row, information);
        }

        @Override
        public String toString() {
            return String.format("ExpectedHex{column=%d, row=%d, information=%s}", column, row, information);
        }
    }
}
