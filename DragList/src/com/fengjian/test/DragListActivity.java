package com.fengjian.test;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


/**
 * �Զ���ListView,����ק,��CheckBox
 * 
 * ��קʱ,�ɱ���ԭ�ȵ�ѡ��״̬,�ɻ�ȡ��ǰѡ���id
 * 
 * �������ʻ�������ϵ,QQ��374076832
 * 
 * @version 1.0 (��һ���汾,���ӷ�ҳ����)
 * @author znn
 */
public class DragListActivity extends Activity {
	
    /**��ԭʼ�����ݼ���**/
	List<Sheet> srclist;
	/**ListView ��ʾ���ݼ���,��Ϊ�����϶�,��ı�ԭʼλ��**/
    List<Sheet> list;
    /**��¼״̬��Ϣ**/
    SparseArray<StateInfo> map;
    /**�����ť**/
    Button button;
    /**
     * ��ʼ��ģ������
     */
    public void initData(){
    	map = new SparseArray<StateInfo>();
        list = new ArrayList<Sheet>();
      
        for(int i=0; i<30; i++){
        	list.add(new Sheet(i,"url:"+i,"Item Name-"+i));
        	map.put(i,  new StateInfo(i));
        }
        srclist = new ArrayList<Sheet>(list);
    }
    
    /**
     * ��ʼ��View
     */
    public void initView(){
    	 setContentView(R.layout.drag_list_activity);
    	 button = (Button)this.findViewById(R.id.button);
    	 DragListView dragListView = (DragListView)findViewById(R.id.drag_list);
         dragListView.setAdapter(new DragListAdapter(this, list,map));
         dragListView.setMap(map);
    }
   
    /**
     * ��ʼ����������
     */
    public void initListener(){
    	  button.setOnClickListener(new View.OnClickListener() {
  			@Override
  			public void onClick(View v) {
  				// TODO Auto-generated method stub
  				StringBuffer buf = new StringBuffer("��ǰѡ��ID��");
  				for(int i = 0, nsize = map.size(); i < nsize; i++) {
  					StateInfo obj = map.valueAt(i);
  					 if(obj.isSelected){
  							Sheet sheet = srclist.get(obj.srcPosition);
  							buf.append(" {");
  							buf.append(sheet.id).append("}");
  						}
  					}
  				Toast.makeText(getApplicationContext(), buf.toString(), 0).show();
  			}
  		});
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
        initListener();
    }
    
    public static class DragListAdapter extends ArrayAdapter<Sheet>{
    	SparseArray<StateInfo> map ;
    	LayoutInflater layoutInflater;
        public DragListAdapter(Context context, List<Sheet> objects,SparseArray<StateInfo> map ) {
        	super(context, 0, objects);
            this.map = map;
            layoutInflater = LayoutInflater.from(context);
        }
        
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
        	
        	ViewHolder holder = null;
        	/**
        	 * ��view == nullʱ,���м����holder,�洢�´������������
        	 * ��view !=nullʱ,ֱ��ȡ������view�е����
        	 */
            if(view == null){
            	holder = new ViewHolder();
                view = layoutInflater.inflate(R.layout.drag_list_item, null);
                holder.box =  (CheckBox)view.findViewById(R.id.selected);
                holder.textView= (TextView)view.findViewById(R.id.drag_list_item_text);
                view.setTag(holder);
            }else{
            	holder = ((ViewHolder)view.getTag());
            }
            CheckBox box =  holder.box ;
            TextView textView = holder.textView;
            box.setTag(position);
            box.setChecked(map.get(position).isSelected);
            textView.setText(getItem(position).name);
           
            box.setOnClickListener(new View.OnClickListener() {  
                @Override  
                public void onClick(View view) {  
                    CheckBox cb = (CheckBox)view;  
                    map.get((Integer)view.getTag()).isSelected =cb.isChecked();
                }  
            });  
            return view;
        }

    }
}

class Sheet{
	public int id;
	public String url;
	public String name;
	public Sheet(int id, String url,String name) {
		super();
		this.id = id;
		this.url = url;
		this.name = name;
	}
	
}

class StateInfo{
	public StateInfo(int srcPosition) {
		super();
		this.srcPosition = srcPosition;
		isSelected = false;
	}
	/**ԭʼ����**/
	public int srcPosition;
	/**�Ƿ�ѡ��**/
	public boolean isSelected;
}

class ViewHolder{
	public TextView textView ;
	public CheckBox box;
}
