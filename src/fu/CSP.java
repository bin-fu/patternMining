package fu;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * a class for represent pattern
 */


public class CSP {
  /**
   * sequences data
   */
  Sequences m_seqs;

  protected int m_max_length;
  protected double m_min_sup_p;
  protected double m_min_sup_n;

  protected LinkedList<ContrastPattern> m_patterns;


  public CSP(String file_path, boolean header, String sep) throws IOException {
    m_seqs = new Sequences(file_path,header,sep,true);
    m_max_length = 5;
    m_min_sup_p = 0.03;
    m_min_sup_n = 0.03;
    m_patterns = new LinkedList<ContrastPattern>();
  }

  protected void append_patterns(List<ContrastPattern> patterns) {
    //
    Iterator<ContrastPattern> iterator = patterns.iterator();
    while (iterator.hasNext()) {
      m_patterns.addLast(iterator.next());
    }
  }


  protected int[] rank_patterns(){
    // get all lift value
    double[] sups = new double[m_patterns.size()];
    for (int i = 0; i < m_patterns.size(); i++) {
      sups[i] = m_patterns.get(i).m_lift;
    }
    int[] orderedIndex = utils.order(sups, true);
    return orderedIndex;
  }


  public void store_patterns(String file_path) throws IOException {
    int[] orderedIndex = rank_patterns();
    StringBuilder sb = new StringBuilder();
    sb.append("patterns: positive_support: negative_support: lift\n");

    BufferedWriter writer = new BufferedWriter(new FileWriter(file_path));
    for(int i=0;i<orderedIndex.length;i++){
      ContrastPattern p = m_patterns.get(orderedIndex[i]);
      sb.append(m_seqs.ids_to_events(p.m_pattern));
      sb.append(": ");
      sb.append(p.toString());
      sb.append('\n');
    }
    writer.write(sb.toString());
    writer.flush();
    writer.close();
  }


  public void search_patterns(double min_sup_p, double min_sup_n, int max_length) {
    this.m_min_sup_p = min_sup_p;
    this.m_min_sup_n = min_sup_n;
    this.m_max_length = max_length;
    csp_span(new LinkedList<Integer>(), 0, m_seqs.init_projectedDatabase());
  }


  protected void csp_span(LinkedList<Integer> prefix, int length, LinkedList<int[]> projected) {

    if (projected.size() < m_min_sup_p || projected.size() < m_min_sup_n) {
      return;
    } else {
      // step 1: find all frequent 1-length patterns
      LinkedList<ContrastPattern> pattern_1 = find_frequent_item(projected);
      if (pattern_1.size() <= 0) {
        return;
      } else {
        if (prefix.size() > 0) {  // add prefix to each length-1 pattern found locally
          for (int i = 0; i < pattern_1.size(); i++) {
            ContrastPattern p = pattern_1.get(i).add_prefix(prefix);
            pattern_1.set(i, p);
            m_patterns.addLast(p);
          }
        } else {
          append_patterns(pattern_1);
        }
        if ((length + 1) >= m_max_length)
          return;
        else {
          for (ContrastPattern p : pattern_1) {
            LinkedList<Integer> this_prefix = p.m_pattern;
            int this_length = this_prefix.size();
            LinkedList<int[]> this_projected = m_seqs.get_new_projected(this_prefix, projected);
            csp_span(this_prefix, this_length, this_projected);
          }
        }
      }
    }
  }


  /**
   * find the frequent 1-length sequence from the projected database
   *
   * @param projectedDatabase
   * @return
   */
  protected LinkedList<ContrastPattern> find_frequent_item(LinkedList<int[]> projectedDatabase) {
    // step 1: item count
    int num_events = m_seqs.m_num_events;
    int[] sup_p = new int[num_events];
    for (int i = 0; i < num_events; i++)
      sup_p[i] = 0;

    int[] sup_n = new int[num_events];
    for (int i = 0; i < num_events; i++)
      sup_n[i] = 0;

    for (int[] this_one : projectedDatabase) {
      int seq_id = this_one[0];
      int start_pos = this_one[1];
      int[] this_seq = m_seqs.m_sequences[seq_id];
      for (int j = 0; j < num_events; j++) {
        int pos = utils.first_pos(j, this_seq, start_pos);
        if (pos >= start_pos){
          if(m_seqs.m_labels[seq_id]==1){
            sup_p[j]++;
          } else {
            sup_n[j]++;
          }
        }
      }
    }

    for(int i =0; i<num_events;i++){
      sup_p[i] = sup_p[i]/m_seqs.m_num_seq_p;
      sup_n[i] = sup_n[i]/m_seqs.m_num_seq_n;
    }

    // step 2: return the frequent 1-length items
    LinkedList<ContrastPattern> result = new LinkedList<ContrastPattern>();
    for (int i = 0; i < num_events; i++) {
      if (sup_p[i]>=m_min_sup_p && sup_n[i]>= m_min_sup_n) {
        int[] pattern = {i};
        result.addLast(new ContrastPattern(pattern, sup_p[i],sup_n[i]));
      }
    }
    return result;
  }


  public static void main(String[] args) throws IOException {
    CSP ps = new CSP("D:\\seq.txt", true, ",");
    ps.search_patterns(0.03, 0.03, 5);
    ps.store_patterns("D:\\patterns.txt");
  }
}

