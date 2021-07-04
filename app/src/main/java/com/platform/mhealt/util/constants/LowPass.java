package com.platform.mhealt.util.constants;

public class LowPass {
    private double prev_value;
    private long prev_date;
    private boolean stated;
    private final double tau = 1.0;
    public LowPass(){
        this.prev_value = 0;
        this.stated = true;
    }
    public double add_pass(double value){
        this.prev_value = this.low_pass(this.prev_value, value);
        return this.prev_value;
    }
    private double low_pass(double prev, double current){
        double a = this.get_a();
        return current+ a*(current-prev);
    }

    private double  get_a(){
        if (this.stated){
            this.prev_date = System.currentTimeMillis();
            this.stated = false;
            return 0;
        } else {
            long new_Date = System.currentTimeMillis();
            double T = (new_Date - this.prev_date)/1000;
            this.prev_date = new_Date;
            return T/(tau + T);
        }
    }
}
