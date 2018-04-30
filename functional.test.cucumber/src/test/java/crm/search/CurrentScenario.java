package crm.search;

import cucumber.api.Scenario;

public class CurrentScenario {

	private static final ThreadLocal<Scenario> CURRENT = new ThreadLocal<Scenario>();

    public static Scenario getCurrentScenario() {
        return CURRENT.get();
    }

    public static void setCurrentScenario(Scenario scenario) {
    	System.out.println("SCENARIO: "+scenario.getName());
        CURRENT.set(scenario);
    }
}