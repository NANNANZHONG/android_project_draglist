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
 * 自定义ListView,可拖拽,带CheckBox
 * 
 * 拖拽时,可保持原先的选中状态,可获取当前选择的id
 * 
 * 如有疑问或建议请联系,QQ：374076832
 * 
 * @version 1.0 (下一个版本,增加分页加载)
 * @author znn
 */
public class DragListActivity extends Activity {
	
    /**最原始的数据集合**/
	List<Sheet> srclist;
	/**ListView 显示数据集合,因为可以拖动,会改变原始位置**/
    List<Sheet> list;
    /**记录状态信息**/
    SparseArray<StateInfo> map;
    /**点击按钮**/
    Button button;
    /**
     * 初始化模拟数据
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
     * 初始化View
     */
    public void initView(){
    	 setContentView(R.layout.drag_list_activity);
    	 button = (Button)this.findViewById(R.id.button);
    	 DragListView dragListView = (DragListView)findViewById(R.id.drag_list);
         dragListView.setAdapter(new DragListAdapter(this, list,map));
         dragListView.setMap(map);
    }
   
    /**
     * 初始化监听函数
     */
    public void initListener(){
    	  button.setOnClickListener(new View.OnClickListener() {
  			@Override
  			public void onClick(View v) {
  				// TODO Auto-generated method stub
  				StringBuffer buf = new StringBuffer("当前选中ID：");
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
        	 * 当view == null时,用中间变量holder,存储新创建出来的组件
        	 * 当view !=null时,直接取出放在view中的组件
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
	/**原始坐标**/
	public int srcPosition;
	/**是否选中**/
	public boolean isSelected;
}

class ViewHolder{
	public TextView textView ;
	public CheckBox box;
}
