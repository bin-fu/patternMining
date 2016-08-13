package fu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by ben on 8/12/2016.
 */
public class Sequences {
  /** */
  public int[][] m_sequences;
  public int[] m_labels = null;
  public int m_num_seq_p = 0;
  public int m_num_seq_n = 0;

  public int m_num_seqs = 0;
  public int m_num_events = 0;

  public HashMap<String, Integer> m_event_id;
  public HashMap<Integer, String> m_id_event;


  protected void dict_item(String file_path, boolean header, String sep, boolean has_label) throws
    IOException {
    m_event_id = new HashMap<String, Integer>();
    m_id_event = new HashMap<Integer, String>();

    BufferedReader br = new BufferedReader(new FileReader(file_path));
    if (header == true)
      br.readLine();
    m_num_seqs = 0;
    String line;
    String[] parsed;
    int key_index = 0;
    int start_pos = 0;
    if(has_label==true)
      start_pos = 1;
    while ((line = br.readLine()) != null) {
      m_num_seqs++;
      parsed = line.trim().toLowerCase().split(sep);
      for (int i = start_pos; i < parsed.length; i++) {
        if (m_event_id.get(parsed[i]) == null) {
          m_event_id.put(parsed[i], key_index++);
        }
      }
    }
    br.close();

    m_num_events = m_event_id.size();
    for (String key : m_event_id.keySet()) {
      m_id_event.put(m_event_id.get(key), key);
    }
  }


  protected void store_sequences(String file_path, boolean header, String sep, boolean has_label)
    throws IOException {
    m_sequences = new int[m_num_seqs][];
    BufferedReader br = new BufferedReader(new FileReader(file_path));
    if (header == true)
      br.readLine();
    String line;
    String[] parsed;
    int seq_index = 0;
    if(has_label == false){
      while ((line = br.readLine()) != null) {
        parsed = line.trim().toLowerCase().split(sep);
        int[] this_seq = new int[parsed.length];
        for(int i = 0; i<parsed.length;i++)
          this_seq[i] = m_event_id.get(parsed[i]);
        m_sequences[seq_index++] = this_seq;
      }
    }else {
      while ((line = br.readLine()) != null) {
        parsed = line.trim().toLowerCase().split(sep);
        m_labels[seq_index] = Integer.parseInt(parsed[0]);
        if(m_labels[seq_index]==1)
          m_num_seq_p++;
        else
          m_num_seq_n++;
        int[] this_seq = new int[parsed.length-1];
        for(int i = 0; i<parsed.length-1;i++)
          this_seq[i] = m_event_id.get(parsed[i+1]);
        m_sequences[seq_index++] = this_seq;
      }
    }
    br.close();
  }

  public LinkedList<int[]> init_projectedDatabase(){
    LinkedList<int[]> result;
    result = new LinkedList<int[]>();
    for (int i = 0; i < m_num_seqs; i++) {
      result.addLast(new int[]{i, 0});
    }
    return result;
  }

  protected String ids_to_events(LinkedList<Integer> ids) {
    StringBuilder sb = new StringBuilder();
    Iterator<Integer> iter = ids.iterator();
    while (iter.hasNext()) {
      int id = iter.next();
      sb.append(m_id_event.get(id));
      sb.append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }


  protected LinkedList<int[]> get_new_projected(LinkedList<Integer> prefix, LinkedList<int[]> current_projected) {
    //
    int item = prefix.getLast();
    LinkedList<int[]> result = new LinkedList<int[]>();
    for (int[] this_projected : current_projected) {
      int seq_id = this_projected[0];
      int start_pos = this_projected[1];
      int first = utils.first_pos(item, m_sequences[seq_id], start_pos);
      if (first == -1 | first == (m_sequences[seq_id].length - 1)) {
        continue;
      } else {
        result.addLast(new int[]{seq_id, (first + 1)});
      }
    }
    return result;
  }


  public Sequences(String file_path, boolean header, String sep) throws IOException {
    new Sequences(file_path,header,sep,false);
  }


  public Sequences(String file_path, boolean header, String sep, boolean has_label) throws
    IOException {
    dict_item(file_path, header, sep, has_label);
    store_sequences(file_path, header, sep, has_label);
  }

  public static void main(String[] args) throws IOException {
    // simple test here
  }
}