package SincronizacionHilos;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * @author Feercha
 */
public class ProgramaBanco {

    
    public static void main(String[] args) {
        Banco b = new Banco();
        
        for(int i=0;i<100;i++){
            EjecutarTransferencias et = new EjecutarTransferencias(b,i,2000);
            
            
            Thread t = new Thread(et);
            t.start();
        }
    }
    
}
//Se crea el banco
class Banco{
    private final double[] cuentas;
    private Lock cierre = new ReentrantLock();
    private Condition saldoSuficiente;
    
    public Banco(){
        cuentas = new double[100];
        
        for(int i=0; i<cuentas.length;i++){
            cuentas[i]=2000;
        }
        saldoSuficiente=cierre.newCondition();
    }
    
    //Método en el que se crea una transferencia
    public void tranferencia(int origen, int destino, double monto)throws InterruptedException{
        
        cierre.lock();  //bloquea el código
        
        try{
        while(cuentas[origen]<=monto){
            saldoSuficiente.await();
            //return;
        }
        
        System.out.println(Thread.currentThread());
        
        cuentas[origen]-=monto;  //resta cantidad en la cuenta de origen
        
        System.out.printf("%10.2f de %d para %d"+ "\n", monto, origen, destino);
        
        cuentas[destino]+=monto;  // ingresa la cantidad a la cuenta de destino
        
        System.out.printf("Saldo total: %10.2f", getSumaCuentas());
        
        saldoSuficiente.signalAll(); //Avisar a los hilos en espera si necesitan la anterior tranferencia
        }finally{
            cierre.unlock();   //desbloquea el código
        }
    }
    
    //Método para obtener la suma de todas las cuentas
    public double getSumaCuentas(){
        double sumaCuentas=0;
        
        for(double x: cuentas){
            sumaCuentas+=x;
        }
        return sumaCuentas;
    }
}

//Clase para que se ejecuten todas las tranferencias
class EjecutarTransferencias implements Runnable{
    private Banco banco;
    private int deLaCuenta;
    private double cantidadMax;
    
    public EjecutarTransferencias(Banco b, int de, double max){
        banco=b;
        deLaCuenta=de;
        cantidadMax=max;
    }

    @Override
    public void run() {
        while(true){
            int paraLaCuenta = (int)(100*Math.random());  //Se crean numeros random para utilizar una cuenta
            double cantidad=cantidadMax*Math.random();
            
            try {
                banco.tranferencia(deLaCuenta, paraLaCuenta, cantidad);
            
                Thread.sleep((int)(Math.random()*10));
            } catch (InterruptedException ex) {
                
            }
        }
    }   
}

