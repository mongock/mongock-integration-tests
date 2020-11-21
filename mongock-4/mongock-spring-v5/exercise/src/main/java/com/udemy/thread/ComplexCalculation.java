package com.udemy.thread;

import java.math.BigInteger;

public class ComplexCalculation {

    public BigInteger calculateResult(BigInteger base1,
                                      BigInteger power1,
                                      BigInteger base2,
                                      BigInteger power2)  {

        PowerCalculatingThread thread1 = new PowerCalculatingThread(base1, power1);
        thread1.setName("power1_thread");
        PowerCalculatingThread thread2 = new PowerCalculatingThread(base2, power2);
        thread2.setName("power2_thread");

        thread1.start();
        thread2.start();


        try {
            thread1.join(2000);
            thread2.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(thread1.isNotYetFinishedCalculation()) {
            thread1.interrupt();
        }
        if(thread2.isNotYetFinishedCalculation()) {
            thread2.interrupt();
        }

        return thread1.getResult().add(thread2.getResult());
    }

    private static class PowerCalculatingThread extends Thread {
        private BigInteger result = BigInteger.ONE;
        private BigInteger base;
        private BigInteger power;
        private boolean finishedCalculation = false;

        PowerCalculatingThread(BigInteger base, BigInteger power) {
            this.base = base;
            this.power = power;
        }

        @Override
        public void run() {
            BigInteger tempResult = result;
            for(int i = 0 ; i < power.intValue() ; i++) {
                if(Thread.currentThread().isInterrupted()) {
                    System.out.println(String.format("Thread [%s] interrupted", this.getName()));
                    return;
                }
                tempResult = tempResult.multiply(base);
            }
            result = tempResult;
            finishedCalculation = true;
        }

        BigInteger getResult() { return result; }

        boolean isNotYetFinishedCalculation() {
            return !finishedCalculation;
        }
    }
}
