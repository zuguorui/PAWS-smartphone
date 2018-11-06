package bashima.cs.unc.seus.Util;

public class Maths {

   /** sqrt(a^2 + b^2) without under/overflow. **/

   public static double hypot(double a, double b) {
      double r;
      if (Math.abs(a) > Math.abs(b)) {
         r = b/a;
         r = Math.abs(a)*Math.sqrt(1+r*r);
      } else if (b != 0) {
         r = a/b;
         r = Math.abs(b)*Math.sqrt(1+r*r);
      } else {
         r = 0.0;
      }
      return r;
   }

   /**
    * Finds the next power of two. If the value is exactly on a power of two, then it returns
    * that value. Value must be > 0
    * @param value
    * @return
    */
   public static int nextpow2(double value) {
      int rounded_val = (int) Math.ceil(value);
      int nextPower = Integer.highestOneBit(rounded_val);

      // Left shift if the original value is greater than the value of the highest one bit.
      if(rounded_val > nextPower) {
         nextPower = nextPower << 1;
      }
      return nextPower;
   }
}
