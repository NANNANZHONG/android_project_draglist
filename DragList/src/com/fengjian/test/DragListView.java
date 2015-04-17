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
    private ImageView dragImageView;//����ק�����ʵ����һ��ImageView
    private int dragSrcPosition;//��ָ�϶���ԭʼ���б��е�λ��
    private int dragPosition;//��ָ�϶���ʱ�򣬵�ǰ�϶������б��е�λ��
    
    private int dragPoint;//�ڵ�ǰ�������е�λ��
    private int dragOffset;//��ǰ��ͼ����Ļ�ľ���(����ֻʹ����y������)
    
    private WindowManager windowManager;//windows���ڿ�����
    private WindowManager.LayoutParams windowParams;//���ڿ�����ק�����ʾ�Ĳ���
    
    private int scaledTouchSlop;//�жϻ�����һ������
    private int upScrollBounce;//�϶���ʱ�򣬿�ʼ���Ϲ����ı߽�
    private int downScrollBounce;//�϶���ʱ�򣬿�ʼ���¹����ı߽�
    
    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }
    
    //����touch�¼�����ʵ���Ǽ�һ�����
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	//����down�¼�
        if(ev.getAction()==MotionEvent.ACTION_DOWN){
            int x = (int)ev.getX();
            int y = (int)ev.getY();
            //ѡ�е�������λ�ã�ʹ��ListView�Դ���pointToPosition(x, y)����
            dragSrcPosition = dragPosition = pointToPosition(x, y);
          //�������Чλ��(�����߽磬�ָ��ߵ�λ��)������
            if(dragPosition==AdapterView.INVALID_POSITION){
                return super.onInterceptTouchEvent(ev);
            }
          //��ȡѡ����View
            //getChildAt(int position)��ʾdisplay�ڽ����positionλ�õ�View
            //getFirstVisiblePosition()���ص�һ��display�ڽ����view��adapter��λ��position��������0��Ҳ������4
            ViewGroup itemView = (ViewGroup) getChildAt(dragPosition-getFirstVisiblePosition());
            
            
            //dragPoint���λ���ڵ��View�ڵ����λ��
            //dragOffset��Ļλ�ú͵�ǰListViewλ�õ�ƫ����������ֻ�õ�y�����ϵ�ֵ
            //�������������ں����϶��Ŀ�ʼλ�ú��ƶ�λ�õļ���
            dragPoint = y - itemView.getTop();
            
            dragOffset = (int) (ev.getRawY() - y);
          //��ȡ�ұߵ��϶�ͼ�꣬����Ժ��������ק������
            View dragger = itemView.findViewById(R.id.drag_list_item_image);
            if(dragger!=null&&x>dragger.getLeft()-20){
            	//׼���϶�
                //��ʼ���϶�ʱ��������
                //scaledTouchSlop�������϶���ƫ��λ(һ��+-10)
                //upScrollBounce������Ļ���ϲ�(����1/3����)���߸��ϵ�����ִ���϶��ı߽磬downScrollBounceͬ����
                upScrollBounce = Math.min(y-scaledTouchSlop, getHeight()/3);
                downScrollBounce = Math.max(y+scaledTouchSlop, getHeight()*2/3);
                
              //����DrawingcacheΪtrue�����ѡ�����Ӱ��bm�����Ǻ��������϶����ĸ�ͷ��
                itemView.setDrawingCacheEnabled(true);
                Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache());
                startDrag(bm, y);
            }
            return false;
         }
         return super.onInterceptTouchEvent(ev);
    }

    /**
     * �����¼�
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
        //Ҳ������ѡ�е�Ч��
        return super.onTouchEvent(ev);
    }
    
    /**
     * ׼���϶�����ʼ���϶����ͼ��
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
     * ֹͣ�϶���ȥ���϶����ͷ��
     */
    public void stopDrag(){
        if(dragImageView!=null){
            windowManager.removeView(dragImageView);
            dragImageView = null;
        }
    }
    
    /**
     * �϶�ִ�У���Move������ִ��
     * @param y
     */
    public void onDrag(int y){
        if(dragImageView!=null){
            windowParams.alpha = 0.8f;
            windowParams.y = y - dragPoint + dragOffset;
            windowManager.updateViewLayout(dragImageView, windowParams);
        }
        //Ϊ�˱��⻬�����ָ��ߵ�ʱ�򣬷���-1������
        int tempPosition = pointToPosition(0, y);
        if(tempPosition!=INVALID_POSITION){
            dragPosition = tempPosition;
        }
        
        //����
        int scrollHeight = 0;
        if(y<upScrollBounce){
            scrollHeight = 8;//�������Ϲ���8�����أ�����������Ϲ����Ļ�
        }else if(y>downScrollBounce){
            scrollHeight = -8;//�������¹���8�����أ�������������Ϲ����Ļ�
        }
        
        if(scrollHeight!=0){
            //���������ķ���setSelectionFromTop()
            setSelectionFromTop(dragPosition, getChildAt(dragPosition-getFirstVisiblePosition()).getTop()+scrollHeight);
        }
    }
    
    /**
     * �϶����µ�ʱ��
     * @param y
     */
    public void onDrop(int y){
        
        //Ϊ�˱��⻬�����ָ��ߵ�ʱ�򣬷���-1������
        int tempPosition = pointToPosition(0, y);
        if(tempPosition!=INVALID_POSITION){
            dragPosition = tempPosition;
        }
        
        //�����߽紦��
        if(y<getChildAt(0).getTop()){
            //�����ϱ߽�
            dragPosition = 1;
        }else if(y>getChildAt(getChildCount()-1).getBottom()){
            //�����±߽�
            dragPosition = getAdapter().getCount()-1;
        }
        
        //���ݽ��� dragPosition:Ŀ��λ��    dragSrcPositionԭ��λ��
        
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
