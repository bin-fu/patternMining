package fu;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * some usrful functions
 */
public class utils {
  /**
   *  find the position of an integer value in an integer array after a particular position. if not
   *  found, return -1
   * @param x
   * @param all
   * @param star_pos
   * @return
   */
  public static int first_pos(int x, int[] all, int star_pos) {
    int result = -1;
    for (int i = star_pos; i < all.length; i++) {
      if (all[i] == x) {
        result = i;
        break;
      }
    }
    return result;
  }

  /**
   *  find the position of an string value in an string array after a particular position. if not
   *  found, return -1
   * @param x
   * @param all
   * @param star_pos
   * @return
   */
  public static int first_pos(String x, String[] all, int star_pos) {
    int result = -1;
    for (int i = star_pos; i < all.length; i++) {
      if (all[i] == x) {
        result = i;
        break;
      }
    }
    return result;
  }

  public int is_pattern(String[] p, String[] seq) {
    int start_pos = 0;
    for (int i = 0; i < p.length; i++) {
      int this_first = first_pos(p[i], seq, start_pos);
      if(this_first == -1){
        return 0;
      } else if(this_first==(p.length-1)&& i<(p.length-1)){
        return 0;
      } else{
        start_pos = this_first + 1;
      }
    }
    return 1;
  }


  public static int[] order(final double[] v){
    int[] result = order(v, false);
    return result;
  }


  public static int[] order(final double[] v, boolean decreasing) {
    //
    Integer[] result = new Integer[v.length];
    for (int i = 0; i < result.length; i++)
      result[i] = i;

    Arrays.sort(result, new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return Double.compare(v[o1], v[o2]);
      }
    });

    int[] ii = new int[v.length];
    if(decreasing == true){
      for (int i = 0; i< v.length; i++)
        ii[i] = result[v.length-1-i];
    }else {
      for (int i = 0; i < v.length; i++)
        ii[i] = result[i];
    }

    return ii;
  }

  public static void main(String[] args) throws IOException {
    // simple test here
  }
}
