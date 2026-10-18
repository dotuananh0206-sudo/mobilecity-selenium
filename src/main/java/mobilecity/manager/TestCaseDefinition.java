package mobilecity.manager;

public record TestCaseDefinition(
        String id,
        String name,
        String feature,
        String className,
        String methodName,
        String precondition,
        String steps,
        String expectedResult) {

    public String selector() {
        return className + "#" + methodName;
    }
}
