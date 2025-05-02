public class ConcreteState implements State{
    private final String name;
    public ConcreteState(String name) {
        this.name = name.toUpperCase();
    }
    public String getName() { return name; }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        return name.equalsIgnoreCase(((State) o).getName());
    }
    public int hashCode() { return name.toUpperCase().hashCode(); }
}
