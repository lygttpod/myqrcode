package com.allen.qrcode.db;

import android.content.Context;
import android.util.Log;

import com.allen.qrcode.db.greenrobot.gen.DaoMaster;
import com.allen.qrcode.db.greenrobot.gen.DaoMaster.OpenHelper;
import com.allen.qrcode.db.greenrobot.gen.DaoSession;
import com.allen.qrcode.db.greenrobot.gen.QRresult;
import com.allen.qrcode.db.greenrobot.gen.QRresultDao;
import com.allen.qrcode.db.greenrobot.gen.QRresultDao.Properties;

import java.util.List;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;

public class GreenDaoUtils {

	private static QRresultDao qRresultDao;

	private static DaoMaster daoMaster;
	private static DaoSession daoSession;
	private Context context;

	public GreenDaoUtils(Context context) {
		super();
		// TODO Auto-generated constructor stub
		daoMaster = getDaoMaster(context);
		daoSession = getDaoSession(context);
		qRresultDao = daoSession.getQRresultDao();
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

	/**
	 * 添加数据
	 *
	 * @param result
	 * @param type
	 * @param time
	 */
	public final void insertGreenDao(String result, int type, String time) {
		// 添加一条数据
		QRresult qRresult = new QRresult(null, result, type, time);
		qRresultDao.insert(qRresult);
	}

	/**
	 * 获取数据
	 *
	 * @return
	 */

	public List<QRresult> getQrList() {
		QueryBuilder<QRresult> qBuilder = qRresultDao.queryBuilder();
		qBuilder.orderDesc(Properties.Id);
		List<QRresult> qRresults = qBuilder.list();
		return qRresults;
	}

	public void select(String result) {
		QueryBuilder<QRresult> qb = qRresultDao.queryBuilder();
		qb.where(Properties.Result.eq(result));
		qb.toString();
	}

	/**
	 * 删除数据
	 *
	 * @param pp
	 */
	public void deleteQrResulte(QRresult qRresults) {
		qRresultDao.delete(qRresults);
	}

	public void deletePersonDetail(long id) {
		Log.d("qrcode", "id======" + id);
		qRresultDao.deleteByKey((long) 2);
		// QueryBuilder<QRresult> qb = qRresultDao.queryBuilder();
		// DeleteQuery<QRresult> bd =
		// qb.where(Properties.Id.eq(id)).buildDelete();
		// bd.executeDeleteWithoutDetachingEntities();

	}

	public void deleteAllData() {
		qRresultDao.deleteAll();
	}

	public void deleteOneQrResulte(String result) {
		QueryBuilder<QRresult> qb = qRresultDao.queryBuilder();
		DeleteQuery<QRresult> dq = qb.where(Properties.Result.eq(result))
				.buildDelete();
		dq.executeDeleteWithoutDetachingEntities();

	}

}
