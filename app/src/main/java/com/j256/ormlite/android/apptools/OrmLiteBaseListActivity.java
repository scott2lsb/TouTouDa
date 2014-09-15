package com.j256.ormlite.android.apptools;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;

import com.j256.ormlite.support.ConnectionSource;

/**
 * Base class to use for Tab activities in Android.
 * 
 * For more information, see {@link OrmLiteBaseActivity}.
 * 
 * @author graywatson, kevingalligan
 */
public abstract class OrmLiteBaseListActivity<H extends OrmLiteSqliteOpenHelper> extends ListActivity {

	private volatile H helper;
	private volatile boolean created = false;
	private volatile boolean destroyed = false;

	/**
	 * Get a helper for this action.
	 */
	public H getHelper() {
		if (helper == null) {
			if (!created) {
				throw new IllegalStateException("A call has not been made to onCreate() yet so the helper is null");
			} else if (destroyed) {
				throw new IllegalStateException(
						"A call to onDestroy has already been made and the helper cannot be used after that point");
			} else {
				throw new IllegalStateException("Helper is null for some unknown reason");
			}
		} else {
			return helper;
		}
	}

	/**
	 * Get a connection source for this action.
	 */
	public ConnectionSource getConnectionSource() {
		return getHelper().getConnectionSource();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (helper == null) {
			helper = getHelperInternal(this);
			created = true;
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseHelper(helper);
		destroyed = true;
	}

	/**
	 * This is called internally by the class to populate the helper object instance. This should not be called directly
	 * by client code unless you know what you are doing. Use {@link #getHelper()} to get a helper instance. If you are
	 * managing your own helper creation, override this method to supply this activity with a helper instance.
	 * 
	 * <p>
	 * <b> NOTE: </b> If you override this method, you most likely will need to override the
	 * {@link #releaseHelper(OrmLiteSqliteOpenHelper)} method as well.
	 * </p>
	 */
	protected H getHelperInternal(Context context) {
		@SuppressWarnings({ "unchecked", "deprecation" })
		H newHelper = (H) OpenHelperManager.getHelper(context);
		return newHelper;
	}

	/**
	 * Release the helper instance created in {@link #getHelperInternal(android.content.Context)}. You most likely will not need to call
	 * this directly since {@link #onDestroy()} does it for you.
	 * 
	 * <p>
	 * <b> NOTE: </b> If you override this method, you most likely will need to override the
	 * {@link #getHelperInternal(android.content.Context)} method as well.
	 * </p>
	 */
	protected void releaseHelper(H helper) {
		OpenHelperManager.releaseHelper();
		this.helper = null;
	}
}
