package com.allen.qrcode.adapter;

import java.util.List;

import com.allen.qrcode.db.greenrobot.gen.QRresult;

import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class QrAdapter extends BaseAdapter {
	List<QRresult> qRresults;

	Context context;

	public QrAdapter(List<QRresult> qRresults, Context context) {
		super();
		this.qRresults = qRresults;
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return qRresults.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View view, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(context).inflate(
					com.allen.myqrcode.R.layout.history_items, null);
			viewHolder.imgType = (ImageView) view
					.findViewById(com.allen.myqrcode.R.id.imgtypeIV);
			viewHolder.resultTV = (TextView) view
					.findViewById(com.allen.myqrcode.R.id.textView_result);
			viewHolder.timeTV = (TextView) view
					.findViewById(com.allen.myqrcode.R.id.textView_time);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		if (qRresults.get(arg0).getType() == 1) {
			viewHolder.imgType
					.setBackgroundResource(com.allen.myqrcode.R.drawable.url72);
		} else {
			viewHolder.imgType
					.setBackgroundResource(com.allen.myqrcode.R.drawable.wenben72);
		}

		viewHolder.resultTV.setText(qRresults.get(arg0).getResult());
		viewHolder.timeTV.setText(qRresults.get(arg0).getDate());

		return view;
	}

	class ViewHolder {
		ImageView imgType;
		TextView resultTV;
		TextView timeTV;
	}

}
