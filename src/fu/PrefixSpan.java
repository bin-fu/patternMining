package fu;

import java.io.*;
import java.util.*;

/**
 *  a class for represent pattern
 * */
public class PrefixSpan {
  Sequences m_seqs;

  protected double m_min_sup;
  protected int m_max_length;

  protected LinkedList<FrequentPattern> m_patterns;


  public PrefixSpan(String file_path, boolean header, String sep) throws IOException {
    m_seqs = new Sequences(file_path, header, sep, false);
    m_max_length = 5;
    m_min_sup = 0.03;
    m_patterns = new LinkedList<FrequentPattern>();
  }

  protected void append_patterns(List<FrequentPattern> patterns) {
    //
    Iterator<FrequentPattern> iterator = patterns.iterator();
    while (iterator.hasNext()) {
      m_patterns.addLast(iterator.next());
    }
  }


  protected int[] rank_patterns(){
    // get all lift value
    double[] sups = new double[m_patterns.size()];
    for (int i = 0; i < m_patterns.size(); i++) {
      sups[i] = m_patterns.get(i).m_sup;
    }
    int[] orderedIndex = utils.order(sups, true);
    return orderedIndex;
  }


  public void store_patterns(String file_path) throws IOException {
    int[] orderedIndex = rank_patterns();
    StringBuilder sb = new StringBuilder();
    sb.append("patterns: support\n");

    BufferedWriter writer = new BufferedWriter(new FileWriter(file_path));
    for(int i=0; i<orderedIndex.length; i++){
      FrequentPattern p = m_patterns.get(orderedIndex[i]);
      sb.append(m_seqs.ids_to_events(p.m_pattern));
      sb.append(": ");
      sb.append(p.toString());
      sb.append('\n');
    }
    writer.write(sb.toString());
    writer.flush();
    writer.close();
  }



  /**
   * find the frequent 1-length sequence from the projected database
   *
   * @param projectedDatabase
   * @return
   */
  protected LinkedList<FrequentPattern> find_frequent_item(LinkedList<int[]> projectedDatabase) {
    // step 1: item count
    int num_events = m_seqs.m_num_events;
    double [] item_sup = new double[num_events];
    for (int i = 0; i < num_events; i++)
      item_sup[i] = 0;

    for (int[] this_one : projectedDatabase) {
      int seq_id = this_one[0];
      int start_pos = this_one[1];
      int[] this_seq = m_seqs.m_sequences[seq_id];
      for (int j = 0; j < num_events; j++) {
        int pos = utils.first_pos(j, this_seq, start_pos);
        if (pos >= start_pos){
          item_sup[j]++;
        }
      }
    }

    for(int i =0; i<num_events;i++){
      item_sup[i] = item_sup[i]/m_seqs.m_num_seqs;
    }

    // step 2: return the frequent 1-length items
    LinkedList<FrequentPattern> result = new LinkedList<FrequentPattern>();
    for (int i = 0; i < num_events; i++) {
      if (item_sup[i]>= m_min_sup) {
        int[] pattern = {i};
        result.addLast(new FrequentPattern(pattern, item_sup[i]));
      }
    }
    return result;
  }


  public void search_patterns(double min_sup, int max_length) {
    this.m_min_sup = min_sup;
    this.m_max_length = max_length;
    prefix_span(new LinkedList<Integer>(), 0, m_seqs.init_projectedDatabase());
  }


  protected void prefix_span(LinkedList<Integer> prefix, int length, LinkedList<int[]> projected) {

    if (projected.size() < m_min_sup) {
      return;
    } else {
      // step 1: find all frequent 1-length patterns
      LinkedList<FrequentPattern> pattern_1 = find_frequent_item(projected);
      if (pattern_1.size() <= 0) {
        return;
      } else {
        if (prefix.size() > 0) {  // add prefix to each length-1 pattern found locally
          for (int i = 0; i < pattern_1.size(); i++) {
            FrequentPattern p = pattern_1.get(i).add_prefix(prefix);
            pattern_1.set(i, p);
            m_patterns.addLast(p);
          }
        } else {
          append_patterns(pattern_1);
        }
        if ((length + 1) >= m_max_length)
          return;
        else {
          for (FrequentPattern p : pattern_1) {
            LinkedList<Integer> this_prefix = p.m_pattern;
            int this_length = this_prefix.size();
            LinkedList<int[]> this_projected = m_seqs.get_new_projected(this_prefix, projected);
            prefix_span(this_prefix, this_length, this_projected);
          }
        }
      }
    }
  }


  public static void main(String[] args) throws IOException {
    PrefixSpan ps = new PrefixSpan("D:\\seq.txt", true, ",");
    ps.search_patterns(0.01,5);
    ps.store_patterns("D:\\patterns.txt");
  }
}

