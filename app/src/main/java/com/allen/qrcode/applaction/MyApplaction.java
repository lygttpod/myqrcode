package com.allen.qrcode.applaction;

import android.app.Application;
import android.content.Context;

import com.allen.qrcode.db.HBContant;
import com.allen.qrcode.db.greenrobot.gen.DaoMaster;
import com.allen.qrcode.db.greenrobot.gen.DaoMaster.OpenHelper;
import com.allen.qrcode.db.greenrobot.gen.DaoSession;

public class MyApplaction extends Application {

	private static DaoMaster daoMaster;
	private static DaoSession daoSession;
	private Context context;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		daoMaster = getDaoMaster(getApplicationContext());
		daoSession = getDaoSession(getApplicationContext());
	}

	public static DaoMaster getDaoMaster() {
		return daoMaster;
	}

	public static void setDaoMaster(DaoMaster daoMaster) {
		MyApplaction.daoMaster = daoMaster;
	}

	public static DaoSession getDaoSession() {
		return daoSession;
	}

	public static void setDaoSession(DaoSession daoSession) {
		MyApplaction.daoSession = daoSession;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * 获取DaoMaster
	 *
	 * @param context
	 * @return
	 */
	public static DaoMaster getDaoMaster(Context context) {
		if (daoMaster == null) {

			OpenHelper helper = new DaoMaster.DevOpenHelper(context,
					HBContant.DATABASE_NAME, null);

			daoMaster = new DaoMaster(helper.getWritableDatabase());
		}

		return daoMaster;
	}

	/**
	 * 获取DaoSession
	 *
	 * @param context
	 * @return
	 */
	public static DaoSession getDaoSession(Context context) {
		if (daoSession == null) {
			if (daoMaster == null) {
				daoMaster = getDaoMaster(context);
			}
			daoSession = daoMaster.newSession();
		}
		return daoSession;
	}

}
