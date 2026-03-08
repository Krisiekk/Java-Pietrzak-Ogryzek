package pl.pk.pietrzak.ogryzek.priority;

public class LowPriority implements PriorityLevel {

    @Override
    public int getPriority() {
        return 1;
    }
}