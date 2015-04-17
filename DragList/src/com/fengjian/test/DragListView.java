package com.fengjian.test;


import com.fengjian.test.DragListActivity.DragListAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;


public class DragListView extends ListView {
    
	SparseArray<StateInfo> map;
	public void setMap( SparseArray<StateInfo> map){
		this.map = map;
	}
    private ImageView dragImageView;//被拖拽的项，其实就是一个ImageView
    private int dragSrcPosition;//手指拖动项原始在列表中的位置
    private int dragPosition;//手指拖动的时候，当前拖动项在列表中的位置
    
    private int dragPoint;//在当前数据项中的位置
    private int dragOffset;//当前视图和屏幕的距离(这里只使用了y方向上)
    
    private WindowManager windowManager;//windows窗口控制类
    private WindowManager.LayoutParams windowParams;//用于控制拖拽项的显示的参数
    
    private int scaledTouchSlop;//判断滑动的一个距离
    private int upScrollBounce;//拖动的时候，开始向上滚动的边界
    private int downScrollBounce;//拖动的时候，开始向下滚动的边界
    
    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }
    
    //拦截touch事件，其实就是加一层控制
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	//捕获down事件
        if(ev.getAction()==MotionEvent.ACTION_DOWN){
            int x = (int)ev.getX();
            int y = (int)ev.getY();
            //选中的数据项位置，使用ListView自带的pointToPosition(x, y)方法
            dragSrcPosition = dragPosition = pointToPosition(x, y);
          //如果是无效位置(超出边界，分割线等位置)，返回
            if(dragPosition==AdapterView.INVALID_POSITION){
                return super.onInterceptTouchEvent(ev);
            }
          //获取选中项View
            //getChildAt(int position)显示display在界面的position位置的View
            //getFirstVisiblePosition()返回第一个display在界面的view在adapter的位置position，可能是0，也可能是4
            ViewGroup itemView = (ViewGroup) getChildAt(dragPosition-getFirstVisiblePosition());
            
            
            //dragPoint点击位置在点击View内的相对位置
            //dragOffset屏幕位置和当前ListView位置的偏移量，这里只用到y坐标上的值
            //这两个参数用于后面拖动的开始位置和移动位置的计算
            dragPoint = y - itemView.getTop();
            
            dragOffset = (int) (ev.getRawY() - y);
          //获取右边的拖动图标，这个对后面分组拖拽有妙用
            View dragger = itemView.findViewById(R.id.drag_list_item_image);
            if(dragger!=null&&x>dragger.getLeft()-20){
            	//准备拖动
                //初始化拖动时滚动变量
                //scaledTouchSlop定义了拖动的偏差位(一般+-10)
                //upScrollBounce当在屏幕的上部(上面1/3区域)或者更上的区域，执行拖动的边界，downScrollBounce同理定义
                upScrollBounce = Math.min(y-scaledTouchSlop, getHeight()/3);
                downScrollBounce = Math.max(y+scaledTouchSlop, getHeight()*2/3);
                
              //设置Drawingcache为true，获得选中项的影像bm，就是后面我们拖动的哪个头像
                itemView.setDrawingCacheEnabled(true);
                Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache());
                startDrag(bm, y);
            }
            return false;
         }
         return super.onInterceptTouchEvent(ev);
    }

    /**
     * 触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(dragImageView!=null&&dragPosition!=INVALID_POSITION){
            int action = ev.getAction();
            switch(action){
                case MotionEvent.ACTION_UP:
                    int upY = (int)ev.getY();
                    stopDrag();
                    onDrop(upY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int moveY = (int)ev.getY();
                    onDrag(moveY);
                    break;
                default:break;
            }
            return true;
        }
        //也决定了选中的效果
        return super.onTouchEvent(ev);
    }
    
    /**
     * 准备拖动，初始化拖动项的图像
     * @param bm
     * @param y
     */
    public void startDrag(Bitmap bm ,int y){
        stopDrag();
        
        windowParams = new WindowManager.LayoutParams();
        windowParams.gravity = Gravity.TOP;
        windowParams.x = 0;
        windowParams.y = y - dragPoint + dragOffset;
        windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;

        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(bm);
        windowManager = (WindowManager)getContext().getSystemService("window");
        windowManager.addView(imageView, windowParams);
        dragImageView = imageView;
    }
    
    /**
     * 停止拖动，去除拖动项的头像
     */
    public void stopDrag(){
        if(dragImageView!=null){
            windowManager.removeView(dragImageView);
            dragImageView = null;
        }
    }
    
    /**
     * 拖动执行，在Move方法中执行
     * @param y
     */
    public void onDrag(int y){
        if(dragImageView!=null){
            windowParams.alpha = 0.8f;
            windowParams.y = y - dragPoint + dragOffset;
            windowManager.updateViewLayout(dragImageView, windowParams);
        }
        //为了避免滑动到分割线的时候，返回-1的问题
        int tempPosition = pointToPosition(0, y);
        if(tempPosition!=INVALID_POSITION){
            dragPosition = tempPosition;
        }
        
        //滚动
        int scrollHeight = 0;
        if(y<upScrollBounce){
            scrollHeight = 8;//定义向上滚动8个像素，如果可以向上滚动的话
        }else if(y>downScrollBounce){
            scrollHeight = -8;//定义向下滚动8个像素，，如果可以向上滚动的话
        }
        
        if(scrollHeight!=0){
            //真正滚动的方法setSelectionFromTop()
            setSelectionFromTop(dragPosition, getChildAt(dragPosition-getFirstVisiblePosition()).getTop()+scrollHeight);
        }
    }
    
    /**
     * 拖动放下的时候
     * @param y
     */
    public void onDrop(int y){
        
        //为了避免滑动到分割线的时候，返回-1的问题
        int tempPosition = pointToPosition(0, y);
        if(tempPosition!=INVALID_POSITION){
            dragPosition = tempPosition;
        }
        
        //超出边界处理
        if(y<getChildAt(0).getTop()){
            //超出上边界
            dragPosition = 1;
        }else if(y>getChildAt(getChildCount()-1).getBottom()){
            //超出下边界
            dragPosition = getAdapter().getCount()-1;
        }
        
        //数据交换 dragPosition:目标位置    dragSrcPosition原先位置
        
        if(dragPosition>=0&&dragPosition<getAdapter().getCount()){
            
        	boolean s1 = map.get(dragPosition).isSelected;
        	boolean s2 = map.get(dragSrcPosition).isSelected;
        	
        	int p1 = map.get(dragPosition).srcPosition;
        	int p2 = map.get(dragSrcPosition).srcPosition;
        	
        	map.get(dragSrcPosition).isSelected = s1;
        	map.get(dragSrcPosition).srcPosition = p1;
        	
        	map.get(dragPosition).isSelected = s2;
        	map.get(dragPosition).srcPosition = p2;
            DragListAdapter adapter = (DragListAdapter)getAdapter();
            
            Sheet dragItem = adapter.getItem(dragSrcPosition);
            Sheet dragItem2 = adapter.getItem(dragPosition);
            adapter.remove(dragItem);
            adapter.insert(dragItem, dragPosition);
            adapter.remove(dragItem2);
            adapter.insert(dragItem2, dragSrcPosition);
        }
        
    }
}
