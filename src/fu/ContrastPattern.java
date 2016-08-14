package fu;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bfu on 12/08/16.
 */
class ContrastPattern {
  public LinkedList<Integer> m_pattern;
  public double m_sup_positive;
  public double m_sup_negative;
  public double m_lift;

  public ContrastPattern(int[] pattern, double p_sup, double n_sup) {
    m_pattern = new LinkedList<Integer>();
    for (int i = 0; i < pattern.length; i++)
      m_pattern.addLast(pattern[i]);
    this.m_sup_positive = p_sup;
    this.m_sup_negative = n_sup;
    set_lift();
  }

  protected void set_lift(){
    double lift_a = m_sup_negative/m_sup_positive;
    double lift_b = m_sup_positive/m_sup_negative;
    if(lift_a > lift_b)
      m_lift = lift_a;
    else
      m_lift = lift_b;
  }

  public ContrastPattern add_prefix(List<Integer> prefix) {
    //
    for (int i = (prefix.size() - 1); i >= 0; i--) {
      m_pattern.addFirst(prefix.get(i));
    }
    return this;
  }

  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%.4f", m_sup_positive));
    sb.append(", ");
    sb.append(String.format("%.4f", m_sup_negative));
    sb.append(", ");
    sb.append(String.format("%.4f", m_lift));
    return sb.toString();
  }


  public static void main(String[] args) throws IOException {
    // simple test here
  }
}