package com.example.oturing_final;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MsgAdapter extends ArrayAdapter<Msg>{

	private int mMinItemWidth;
	private int mMaxItemWidth;

	private LayoutInflater mInflater;

	public MsgAdapter(Context context, List<Msg> objects) {
		super(context, -1, objects);
		mInflater = LayoutInflater.from(context);
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final Msg msg = getItem(position);
		ViewHolder viewHolder = null;

		//加载布局
		if(convertView == null){

			convertView = mInflater.inflate(R.layout.item_msg, parent, false);
			viewHolder = new ViewHolder();

			viewHolder.leftLayout = (RelativeLayout) convertView.findViewById(R.id.layout_left);
			viewHolder.rightLayout = (RelativeLayout) convertView.findViewById(R.id.layout_right);
			viewHolder.leftHead = (ImageView) convertView.findViewById(R.id.iv_left_head);
			viewHolder.rightHead = (ImageView) convertView.findViewById(R.id.iv_right_head);
			viewHolder.leftMsg = (TextView) convertView.findViewById(R.id.tv_left_text);

			viewHolder.rightMsg = (TextView) convertView.findViewById(R.id.tv_right_text);
			viewHolder.leftSeconds = (TextView) convertView.findViewById(R.id.id_left_recorder_time);
			viewHolder.rightSeconds = (TextView) convertView.findViewById(R.id.id_right_recorder_time);
			viewHolder.leftLength = convertView.findViewById(R.id.id_left_recorder_length);
			viewHolder.rightLength = convertView.findViewById(R.id.id_right_recorder_length);

			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		//显示相应的控件
		if(msg.getType() == Msg.TYPE_SENT_WORD){

			viewHolder.leftLayout.setVisibility(View.GONE);
			viewHolder.rightLayout.setVisibility(View.VISIBLE);
			viewHolder.rightLength.setVisibility(View.GONE);
			viewHolder.rightSeconds.setVisibility(View.GONE);
			viewHolder.rightMsg.setVisibility(View.VISIBLE);

			viewHolder.rightMsg.setText(msg.getWord());

		} else if(msg.getType() == Msg.TYPE_RECEIVED_WORD){

			viewHolder.rightLayout.setVisibility(View.GONE);
			viewHolder.leftLayout.setVisibility(View.VISIBLE);
			viewHolder.leftLength.setVisibility(View.GONE);
			viewHolder.leftSeconds.setVisibility(View.GONE);
			viewHolder.leftMsg.setVisibility(View.VISIBLE);

			viewHolder.leftMsg.setText(msg.getWord());

		}else if(msg.getType() == Msg.TYPE_SENT_VOICE){

			viewHolder.leftLayout.setVisibility(View.GONE);
			viewHolder.rightLayout.setVisibility(View.VISIBLE);
			viewHolder.rightMsg.setVisibility(View.GONE);
			viewHolder.rightLength.setVisibility(View.VISIBLE);
			viewHolder.rightSeconds.setVisibility(View.VISIBLE);


			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics outMetrics = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(outMetrics);

			mMinItemWidth = (int) (outMetrics.widthPixels*0.15f);
			mMaxItemWidth = (int) (outMetrics.widthPixels*0.7f);

			viewHolder.rightSeconds.setText(Math.round(getItem(position).getTime())+"\"");
			ViewGroup.LayoutParams lp = viewHolder.rightLength.getLayoutParams();
			lp.width = (int) (mMinItemWidth+(mMaxItemWidth/60f*getItem(position).getTime()));

		}
		/* else if(msg.getType() == Msg.TYPE_RECEIVED_VOICE){

			viewHolder.rightLayout.setVisibility(View.GONE);
			viewHolder.leftLayout.setVisibility(View.VISIBLE);
			viewHolder.leftLength.setVisibility(View.VISIBLE);
			viewHolder.leftSeconds.setVisibility(View.VISIBLE);
			viewHolder.leftMsg.setVisibility(View.GONE);

			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics outMetrics = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(outMetrics);

			mMinItemWidth = (int) (outMetrics.widthPixels*0.15f);
			mMaxItemWidth = (int) (outMetrics.widthPixels*0.7f);

			viewHolder.leftSeconds.setText(Math.round(getItem(position).getTime())+"\"");
			ViewGroup.LayoutParams lp = viewHolder.leftLength.getLayoutParams();
			lp.width = (int) (mMinItemWidth+(mMaxItemWidth/60f*getItem(position).getTime()));
		}*/

		return convertView;
	}

	/*@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Msg msg = getItem(position);
		View view;
		ViewHolder viewHolder;

		//	convertView用于之前加载好的布局进行缓存

		if(msg.getType() == Msg.TYPE_RECEIVED){

			viewHolder.leftLayout.setVisibility(View.VISIBLE);
			viewHolder.rightLayout.setVisibility(View.GONE);

			if(msg.getBitmap() == null){

				//显示文字
				viewHolder.leftMsg.setVisibility(View.VISIBLE);
				viewHolder.leftMsg.setText(msg.getContent());
				viewHolder.leftMsgImage.setVisibility(View.GONE);
			} else {

				//显示图片
				viewHolder.leftMsg.setVisibility(View.GONE);
				viewHolder.leftMsgImage.setVisibility(View.VISIBLE);
				viewHolder.leftMsgImage.setImageBitmap(msg.getBitmap());
			}


		}else if(msg.getType() == Msg.TYPE_SENT){

			viewHolder.leftLayout.setVisibility(View.GONE);
			viewHolder.rightLayout.setVisibility(View.VISIBLE);
			viewHolder.rightMsg.setText(msg.getContent());
		}

		return view;
	}*/

	//	存储控件实例
	class ViewHolder{

		TextView  leftSeconds;	//语音时间
		TextView rightSeconds;
		View leftLength;		//语音长度
		View rightLength;

		RelativeLayout leftLayout;		//布局
		RelativeLayout rightLayout;
		ImageView leftHead;		//头像
		ImageView rightHead;
		TextView leftMsg;		//文字消息
		TextView rightMsg;
		//ImageView leftMsgImage;
		//ImageView rightMsgImage;
	}

}
