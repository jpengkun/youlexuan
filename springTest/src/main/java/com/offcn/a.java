package com.offcn;

public class a
{
    private String baseName = "base";
    public a()
    {
        callName();
    }

    public void callName()
    {
        System. out. println(baseName);
    }

    static class Sub extends a
    {
        private String baseName = "sub";
        public void callName()
        {
            System.out.println(123);
            System. out. println (baseName) ;
        }
    }
    public static void main(String[] args)
    {
        a b = new Sub();
    }
}