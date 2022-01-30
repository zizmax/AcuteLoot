package acute.loot;

public interface Module {

    default void preEnable() {};

    void enable();

    void disable();

}
