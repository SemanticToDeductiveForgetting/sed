package pc1.pc2.pc3;

public class FirstProgram
{

    public static void main(String[] args)
    {
        System.out.println("Fibonacci Squence:");
        System.out.print("0 ");
        for (int i = 2; i < 15; i++) {
            System.out.print(i - 1 + i - 2);
            System.out.print(" ");
        }
    }
}
