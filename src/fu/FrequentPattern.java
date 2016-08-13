package fu;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * format of patterns
 */
class FrequentPattern {
  public LinkedList<Integer> m_pattern;
  public double m_sup;


  public FrequentPattern(int[] pattern, double sup) {
    m_pattern = new LinkedList<Integer>();
    for (int i = 0; i < pattern.length; i++)
      m_pattern.addLast(pattern[i]);
    m_sup = sup;
  }


  public FrequentPattern add_prefix(List<Integer> prefix) {
    //
    for (int i = (prefix.size() - 1); i >= 0; i--) {
      m_pattern.addFirst(prefix.get(i));
    }
    return this;
  }


  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append(m_sup);
    return sb.toString();
  }


  public static void main(String[] args) throws IOException {
    // simple test here
  }
}
