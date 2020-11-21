package com.udemy.thread;

public class ThreadMain {

    public static void main(String[] args) throws InterruptedException {

        MyThread myThread = new MyThread();
        myThread.start();

        myThread.interrupt();
    }


    public static class MyThread extends Thread {


        @Override
        public void run() {
                loop();
        }

        private void loop() {
            for(int i=0 ; i < 999999999 ; i++) {
                if(Thread.currentThread().isInterrupted()) {
                    System.out.println("Is interrupted. Bye!");
                    return;
                }
                System.out.println("Loop " +  i);
            }
        }
    }
}
