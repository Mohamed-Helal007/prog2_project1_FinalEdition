import person1.*;
import person2.*;
import person3.facilities.*;
import person3.emergencies.*;
import person4.*;

import java.util.ArrayList;
import java.util.List;

/*
 * Main = the testing class required by the lab (section 13).
 *
 * It runs the 5 required demonstration scenarios and 2 extra ones.
 * Every scenario builds a NEW colony, so scenarios never affect each other.
 *
 * HOW TO SEE IF THE CODE IS WORKING:
 *   - Every check prints [PASS] or [FAIL].
 *   - If a scenario crashes it prints [CRASH] and the program goes on with the next one.
 *   - The last lines show the total and a clear final result.
 *
 * This file uses the classes exactly as the four team members wrote them
 * (same constructors, same method names). It does not change any other file.
 */
public class Main {

    // These fields are filled by buildColony(), so every scenario can use them.
    static Environment environment;
    static Habitat habitat;
    static StorageFacility storage;
    static Laboratory lab;
    static Scientist scientist;
    static Engineer engineer;
    static MedicalOfficer medic;
    static ExplorationRover rover;
    static RepairRobot repairRobot;
    static Drone drone;
    static Equipment scanner;
    static MarsColony colony;
    static Simulation simulation;

    static int passed = 0;
    static int failed = 0;
    static int crashed = 0;

    public static void main(String[] args) {
        System.out.println("MARS-OPS Main started.");

        runScenario(1);
        runScenario(2);
        runScenario(3);
        runScenario(4);
        runScenario(5);
        runScenario(6);
        runScenario(7);

        System.out.println();
        System.out.println("==================================================");
        System.out.println("SUMMARY");
        System.out.println("Checks passed : " + passed);
        System.out.println("Checks failed : " + failed);
        System.out.println("Scenarios crashed : " + crashed);
        if (failed == 0 && crashed == 0) {
            System.out.println("RESULT: ALL CHECKS PASSED - the program works.");
        } else {
            System.out.println("RESULT: SOMETHING IS WRONG - look at the [FAIL] and [CRASH] lines above.");
        }
        System.out.println("==================================================");
        System.out.println("MARS-OPS Main finished.");
    }

    // Runs one scenario. If it crashes, we print the reason and continue.
    static void runScenario(int number) {
        try {
            if (number == 1) scenario1Exploration();
            if (number == 2) scenario2Rejection();
            if (number == 3) scenario3ResourceShortage();
            if (number == 4) scenario4OxygenLeak();
            if (number == 5) scenario5DustStorm();
            if (number == 6) extra1SecondMissionWhileActive();
            if (number == 7) extra2UnresolvedLeak();
        } catch (Exception e) {
            crashed++;
            System.out.println("[CRASH] Scenario " + number + " stopped: " + e);
        }
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

    // Prints [PASS] or [FAIL] for one check and counts it.
    static void check(String what, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("[PASS] " + what);
        } else {
            failed++;
            System.out.println("[FAIL] " + what);
        }
    }

    static void title(String text) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println(text);
        System.out.println("==================================================");
    }

    // Builds a fresh colony with facilities, crew and robots.
    static void buildColony() {
        environment = new Environment();

        // Facilities. The number after the name is the capacity.
        habitat = new Habitat("Habitat A", SurfaceLocation.MARS, 10, 300);
        habitat.addResource(ResourceType.OXYGEN, 300);

        storage = new StorageFacility("Storage 1", 100, SurfaceLocation.MARS);
        storage.addResource(ResourceType.OXYGEN, 200);
        storage.addResource(ResourceType.WATER, 200);
        storage.addResource(ResourceType.SPARE_PARTS, 10);
        storage.addResource(ResourceType.MEDICAL_SUPPLIES, 5);

        lab = new Laboratory("Lab 1", SurfaceLocation.MARS, 5);

        // Fill this list BEFORE creating the ResourcePool, because the pool copies it.
        List<Facility> facilities = new ArrayList<Facility>();
        facilities.add(habitat);
        facilities.add(storage);
        facilities.add(lab);

        ResourcePool pool = new ResourcePool(facilities);
        MissionManager manager = new MissionManager(pool, environment);

        List<CrewMember> crew = new ArrayList<CrewMember>();
        List<Robot> robots = new ArrayList<Robot>();
        colony = new MarsColony(crew, robots, facilities, environment, pool, manager);

        // Crew
        List<String> scientistSkills = new ArrayList<String>();
        scientistSkills.add("geology");
        scientist = new Scientist("S1", "Dr. Sara", habitat, scientistSkills);

        List<String> engineerSkills = new ArrayList<String>();
        engineerSkills.add("repair");
        engineer = new Engineer("E1", "Eng. Omar", habitat, engineerSkills);

        List<String> medicSkills = new ArrayList<String>();
        medicSkills.add("surgery");
        medic = new MedicalOfficer("M1", "Dr. Nour", habitat, medicSkills);

        // Robots
        rover = new ExplorationRover("R1", "Rover 1", habitat);
        scanner = new Equipment("Scanner", true);
        rover.load(scanner);

        repairRobot = new RepairRobot("RR1", "Repair Robot 1", habitat);
        drone = new Drone("D1", "Drone 1", habitat, environment);

        colony.addcrew(scientist);
        colony.addcrew(engineer);
        colony.addcrew(medic);
        colony.addrobot(rover);
        colony.addrobot(repairRobot);
        colony.addrobot(drone);

        simulation = new Simulation(colony);
    }

    // A resource cost with only one resource inside (the mission wants an int array).
    static int[] cost(ResourceType type, int amount) {
        int[] costs = new int[ResourceType.values().length];
        costs[type.ordinal()] = amount;
        return costs;
    }

    // Makes an exploration mission: id, name, region, duration, skill, battery, cost, equipment.
    static ExplorationMission makeExploration(String id, int[] resourceCost) {
        String[] equipmentNeeded = {"Scanner"};
        return new ExplorationMission(id, "Explore Region A", "Region A", 3,
                "geology", 20, resourceCost, equipmentNeeded);
    }

    // The scientist and the rover: the right team for exploration.
    static List<Agent> exploreTeam() {
        List<Agent> team = new ArrayList<Agent>();
        team.add(scientist);
        team.add(rover);
        return team;
    }

    static void printMission(Mission mission) {
        System.out.println("Mission " + mission.getName() + " | state: " + mission.getState()
                + " | progress: " + mission.getElapsed() + "/" + mission.getDuration());
        if (mission.getFailureReason() != null) {
            System.out.println("Failure reason: " + mission.getFailureReason());
        }
    }

    static boolean noActiveMission() {
        return colony.getMissionManager().getActiveMission() == null;
    }

    static boolean has(String text, String word) {
        return text.toLowerCase().contains(word.toLowerCase());
    }

    // ------------------------------------------------------------------
    // Scenario 1: Successful exploration
    // ------------------------------------------------------------------
    static void scenario1Exploration() {
        title("SCENARIO 1: Successful exploration");
        buildColony();

        System.out.println("--- State BEFORE ---");
        System.out.println(colony.generateReport());
        int waterBefore = colony.getTotal(ResourceType.WATER);

        ExplorationMission mission = makeExploration("M1", cost(ResourceType.WATER, 10));
        OperationResult result = colony.submitMission(mission, exploreTeam());
        printMission(mission);

        check("S1 mission accepted", result.isSuccess());
        check("S1 mission is ASSIGNED", mission.getState() == MissionState.ASSIGNED);
        check("S1 scientist and rover are busy", !scientist.isAvailable() && !rover.isAvailable());
        check("S1 water was consumed (" + waterBefore + " -> " + colony.getTotal(ResourceType.WATER) + ")",
                colony.getTotal(ResourceType.WATER) == waterBefore - 10);

        // 1 tick starts the mission, then 3 ticks run it (duration = 3).
        for (int i = 1; i <= 4; i++) {
            simulation.advanceTime(1);
            System.out.println("Time " + simulation.getTime() + ":");
            printMission(mission);
        }

        System.out.println("--- State AFTER ---");
        System.out.println(colony.generateReport());

        check("S1 mission is COMPLETED", mission.getState() == MissionState.COMPLETED);
        check("S1 agents are available again", scientist.isAvailable() && rover.isAvailable());
        check("S1 no active mission left", noActiveMission());
    }

    // ------------------------------------------------------------------
    // Scenario 2: Mission rejection (unsuitable agent)
    // ------------------------------------------------------------------
    static void scenario2Rejection() {
        title("SCENARIO 2: Mission rejection (unsuitable agent)");
        buildColony();

        // An engineer cannot do research, so this team is unsuitable.
        List<Agent> wrongTeam = new ArrayList<Agent>();
        wrongTeam.add(engineer);
        wrongTeam.add(rover);

        ExplorationMission mission = makeExploration("M2", cost(ResourceType.WATER, 10));
        OperationResult result = colony.submitMission(mission, wrongTeam);

        System.out.println("Mission cannot be assigned.");
        System.out.println("Reason: " + result.getReason());
        printMission(mission);

        check("S2 mission rejected", !result.isSuccess());
        check("S2 reason talks about capability", has(result.getReason(), "capability"));
        check("S2 mission is FAILED", mission.getState() == MissionState.FAILED);
        check("S2 mission never became active", noActiveMission());
    }

    // ------------------------------------------------------------------
    // Scenario 3: Resource shortage
    // ------------------------------------------------------------------
    static void scenario3ResourceShortage() {
        title("SCENARIO 3: Resource shortage");
        buildColony();

        int waterBefore = colony.getTotal(ResourceType.WATER);
        System.out.println("Water BEFORE: " + waterBefore);

        // The colony has only 200 water, the mission asks for 5000.
        ExplorationMission mission = makeExploration("M3", cost(ResourceType.WATER, 5000));
        OperationResult result = colony.submitMission(mission, exploreTeam());

        System.out.println("Reason: " + result.getReason());
        printMission(mission);
        int waterAfter = colony.getTotal(ResourceType.WATER);
        System.out.println("Water AFTER: " + waterAfter);

        check("S3 mission rejected", !result.isSuccess());
        check("S3 reason talks about resources", has(result.getReason(), "resource"));
        check("S3 mission is FAILED", mission.getState() == MissionState.FAILED);
        check("S3 nothing was consumed", waterAfter == waterBefore);
        check("S3 water is not negative", waterAfter >= 0);
        check("S3 agents are still available", scientist.isAvailable() && rover.isAvailable());
    }

    // ------------------------------------------------------------------
    // Scenario 4: Emergency handling (oxygen leak)
    // ------------------------------------------------------------------
    static void scenario4OxygenLeak() {
        title("SCENARIO 4: Oxygen leak emergency");
        buildColony();

        int habitatOxygenBefore = habitat.getOxygenLevel();
        int colonyOxygenBefore = colony.getTotal(ResourceType.OXYGEN);
        int partsBefore = colony.getTotal(ResourceType.SPARE_PARTS);
        System.out.println("Habitat oxygen BEFORE: " + habitatOxygenBefore);
        System.out.println("Colony oxygen total BEFORE: " + colonyOxygenBefore);
        System.out.println("Spare parts BEFORE: " + partsBefore);

        OxygenLeak leak = new OxygenLeak(habitat, 50);
        colony.triggerEmergency(leak);

        System.out.println("Emergency type: " + leak.getType());
        System.out.println("Emergency state: " + leak.getState());
        System.out.println("Habitat oxygen AFTER: " + habitat.getOxygenLevel());
        System.out.println("Colony oxygen total AFTER: " + colony.getTotal(ResourceType.OXYGEN));
        System.out.println("Spare parts AFTER: " + colony.getTotal(ResourceType.SPARE_PARTS));
        System.out.println("Habitat status: " + habitat.getStatus());

        check("S4 leak is RESOLVED", leak.getState() == EmergencyState.RESOLVED);
        check("S4 habitat lost 50 oxygen", habitat.getOxygenLevel() == habitatOxygenBefore - 50);
        check("S4 colony oxygen total went down by 50", colony.getTotal(ResourceType.OXYGEN) == colonyOxygenBefore - 50);
        check("S4 response used 1 spare part", colony.getTotal(ResourceType.SPARE_PARTS) == partsBefore - 1);
        check("S4 habitat is still OPERATIONAL", habitat.getStatus() == FacilityStatus.OPERATIONAL);
        check("S4 responders are available again", engineer.isAvailable() && repairRobot.isAvailable());
        check("S4 emergency was written in the log", colony.getEmergencyLog().size() == 1);
    }

    // ------------------------------------------------------------------
    // Scenario 5: Environmental effect (dust storm)
    // ------------------------------------------------------------------
    static void scenario5DustStorm() {
        title("SCENARIO 5: Dust storm");
        buildColony();

        environment.setCondition(EnvironmentCondition.DUST_STORM);
        System.out.println("Environment: " + environment.getCondition());

        ExplorationMission mission = makeExploration("M5", cost(ResourceType.WATER, 10));
        OperationResult result = colony.submitMission(mission, exploreTeam());

        System.out.println("Exploration Mission cannot start.");
        System.out.println("Reason: " + result.getReason());
        printMission(mission);

        check("S5 mission rejected", !result.isSuccess());
        check("S5 reason talks about the dust storm", has(result.getReason(), "dust storm"));
        check("S5 mission is FAILED", mission.getState() == MissionState.FAILED);
        check("S5 mission never became active", noActiveMission());
    }

    // ------------------------------------------------------------------
    // Extra 1: a second mission while one is active
    // ------------------------------------------------------------------
    static void extra1SecondMissionWhileActive() {
        title("EXTRA 1: Second mission while one is active");
        buildColony();

        ExplorationMission first = makeExploration("M6", cost(ResourceType.WATER, 10));
        OperationResult result1 = colony.submitMission(first, exploreTeam());

        ExplorationMission second = makeExploration("M7", cost(ResourceType.WATER, 10));
        OperationResult result2 = colony.submitMission(second, exploreTeam());
        System.out.println("Reason: " + result2.getReason());
        printMission(second);

        check("EXTRA1 first mission accepted", result1.isSuccess());
        check("EXTRA1 second mission rejected", !result2.isSuccess());
        check("EXTRA1 reason says another mission is active", has(result2.getReason(), "active"));
        check("EXTRA1 second mission is FAILED", second.getState() == MissionState.FAILED);
        check("EXTRA1 first mission is still the active one",
                colony.getMissionManager().getActiveMission() == first);
    }

    // ------------------------------------------------------------------
    // Extra 2: unresolved oxygen leak costs twice as much oxygen
    // ------------------------------------------------------------------
    static void extra2UnresolvedLeak() {
        title("EXTRA 2: Unresolved oxygen leak (no responders)");
        buildColony();

        // Nobody is free to respond.
        engineer.setAvailable(false);
        repairRobot.setAvailable(false);

        int habitatOxygenBefore = habitat.getOxygenLevel();
        int partsBefore = colony.getTotal(ResourceType.SPARE_PARTS);
        System.out.println("Habitat oxygen BEFORE: " + habitatOxygenBefore);

        OxygenLeak leak = new OxygenLeak(habitat, 50);
        colony.triggerEmergency(leak);

        System.out.println("Emergency state: " + leak.getState());
        System.out.println("Habitat oxygen AFTER: " + habitat.getOxygenLevel());

        check("EXTRA2 leak is UNRESOLVED", leak.getState() == EmergencyState.UNRESOLVED);
        check("EXTRA2 oxygen was deducted twice (100)", habitat.getOxygenLevel() == habitatOxygenBefore - 100);
        check("EXTRA2 no spare parts were used", colony.getTotal(ResourceType.SPARE_PARTS) == partsBefore);
    }
}
