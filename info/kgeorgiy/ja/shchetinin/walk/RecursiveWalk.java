package info.kgeorgiy.ja.shchetinin.walk;



public class RecursiveWalk {
    private final static HashCalculator calc = new HashCalculator();

    public static void main(String[] args) {
        calc.solve(args, true);
    }
}
