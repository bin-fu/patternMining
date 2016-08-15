package fu;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * the class implement a simple version of the contrast sequential mining algorithm proposed in the following paper:
 *
 *          Zheng, Z., Wei, W., and Liu, C. et a. An effective contrast sequential pattern mining approach to taxpayer
 *           behavior analysis.  World Wide Web, Springer, 2015, pp. 1-19.
 *
 */


public class CSP {
  /** sequences data*/
  Sequences m_seqs;

  /*maximum length (number of items) allowed for patterns*/
  protected int m_max_length;

  /**the minimum support in positive sequences (with label 1) for the patterns*/
  protected double m_min_sup_p;
  /**the minimum support in negative sequences (with label 0) for the patterns*/
  protected double m_min_sup_n;

  /**the found frequent patterns*/
  protected LinkedList<ContrastPattern> m_patterns;


  /**
   *  constructor
   * @param file_path file that contain the sequences data
   * @param header  is the first line in the file is a header
   * @param sep   the delimiter used to separate events in a sequence
   * @throws IOException
   */
  public CSP(String file_path, boolean header, String sep) throws IOException {
    m_seqs = new Sequences(file_path,header,sep,true);
    m_max_length = 5;   //default maximum length of patterns is set to 5
    m_min_sup_p = 0.03; // default value for m_min_sup_p
    m_min_sup_n = 0.03; // deault value for m_min_sup_n
    m_patterns = new LinkedList<ContrastPattern>();  // init the found patterns to null
  }


  /**
   *  append newly found patterns to the set of already found patterns
   * @param patterns
   */
  protected void append_patterns(List<ContrastPattern> patterns) {
    Iterator<ContrastPattern> iterator = patterns.iterator();
    while (iterator.hasNext()) {
      m_patterns.addLast(iterator.next());
    }
  }


  /**
   * rank patterns by their contrast rate decreasingly
   * @return
   */
  protected int[] rank_patterns(){
    // get all lift value
    double[] sups = new double[m_patterns.size()];
    for (int i = 0; i < m_patterns.size(); i++) {
      sups[i] = m_patterns.get(i).m_lift;
    }
    int[] orderedIndex = utils.order(sups, true);
    return orderedIndex;
  }


  /**
   *  store all the found patterns into a external file
   * @param file_path
   * @throws IOException
   */
  public void store_patterns(String file_path) throws IOException {
    int[] orderedIndex = rank_patterns();
    StringBuilder sb = new StringBuilder();
    sb.append("patterns, positive_support, negative_support, contrast_rate\n");

    BufferedWriter writer = new BufferedWriter(new FileWriter(file_path));
    for(int i=0;i<orderedIndex.length;i++){
      ContrastPattern p = m_patterns.get(orderedIndex[i]);
      sb.append(m_seqs.ids_to_events(p.m_pattern));
      sb.append(", ");
      sb.append(p.toString());
      sb.append('\n');
    }
    writer.write(sb.toString());
    writer.flush();
    writer.close();
  }


  /**
   *  this function is the staring point for searching contrast frequent patterns
   * @param min_sup_p  minimum support in sequences with label 1 for the patterns
   * @param min_sup_n  minimum support in sequences with label 0 for the patterns
   * @param max_length  maximum length for the patterns
   */
  public void search_patterns(double min_sup_p, double min_sup_n, int max_length) {
    this.m_min_sup_p = min_sup_p;
    this.m_min_sup_n = min_sup_n;
    this.m_max_length = max_length;
    csp_span(new LinkedList<Integer>(), 0, m_seqs.init_projectedDatabase());
  }


  /**
   * this is a recursive function that find all the patterns in a recursive way. The recursion stops when it cannot
   * find any patterns meet the requirements, i.e., maximum length, minimum support etc. Each recursion consists of two
   * major parts:
   *     (1) find all the frequent items, i.e., length-1 patterns, add the prefix to them and store them as found
   *     patterns
   *     (2) let the next recursion find other patterns with length 2, 3,... max_length.
   *
   * @param prefix
   * @param length
   * @param projected
   */
  protected void csp_span(LinkedList<Integer> prefix, int length, LinkedList<int[]> projected) {

    if (projected.size() < m_min_sup_p*m_seqs.m_num_seq_p || projected.size() < m_min_sup_n*m_seqs.m_num_seq_n) {
      return;
    } else {
      // step 1: find all frequent 1-length patterns
      LinkedList<ContrastPattern> pattern_1 = find_frequent_item(projected);
      if (pattern_1.size() <= 0) {
        return;
      } else {
        // if prefix exist, add prefix to each length-1 pattern and store them to the result set. Otherwise, store
        // them to the result set directly.
        if (prefix.size() > 0) {
          for (int i = 0; i < pattern_1.size(); i++) {
            ContrastPattern p = pattern_1.get(i).add_prefix(prefix);
            pattern_1.set(i, p);
            m_patterns.addLast(p);
          }
        } else {
          append_patterns(pattern_1);
        }
        // if current found patterns have reach the maximum length, then stop. Otherwise, call next recursion to find
        // longer patterns
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
    // step 1: calculate the support for each items in current projected sequences
    int num_events = m_seqs.m_num_events;
    double[] sup_p = new double[num_events];
    for (int i = 0; i < num_events; i++)
      sup_p[i] = 0;

    double[] sup_n = new double[num_events];
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


  /**
   * 5 parameters needs to be given:
   *  1: the file contain the sequences as input
   *  2: the file to store the patterns as output
   *  3: minimum support in positive sequences for desirable patterns
   *  4: minimum support in negative sequences for desirable patterns
   *  5: maximum length for desirable patterns
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
//    String input_file = args[0];
//    String output_file = args[1];
//    double min_sup_p = Double.parseDouble(args[2]);
//    double min_sup_n = Double.parseDouble(args[3]);

    String input_file = "D:\\sequences.csv";
    String output_file = "D:\\patterns.csv";
    double min_sup_p = 0.001;
    double min_sup_n = 0;
    int max_length = 5;`

    CSP ps = new CSP(input_file, true, ",");
    ps.search_patterns(min_sup_p, min_sup_n, max_length);
    ps.store_patterns(output_file);
  }
}

