/*
 * Copyright 2017 Nobuki HIRAMINE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hiramine.fileselectionactivitymultiselecttest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity
{
	// 定数
	private static final int MENUID_FILE                              = 0;    // ファイルメニューID
	private static final int REQUEST_FILESELECT                       = 0;    // リクエストコード
	private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1; // 外部ストレージ読み込みパーミッション要求時の識別コード

	// メンバー変数
	private String m_strInitialDir = Environment.getExternalStorageDirectory().getPath();    // 初期フォルダ

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
	}

	// オプションメニュー生成時
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		super.onCreateOptionsMenu( menu );
		menu.add( 0, MENUID_FILE, 0, "Select File..." );

		return true;
	}

	// オプションメニュー選択時
	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch( item.getItemId() )
		{
			case MENUID_FILE:
				// ファイル選択アクティビティ
				Intent intent = new Intent( this, FileSelectionActivity.class );
				intent.putExtra( FileSelectionActivity.EXTRA_INITIAL_DIR, m_strInitialDir );
				intent.putExtra( FileSelectionActivity.EXTRA_EXT, "jpg; xml" );
				startActivityForResult( intent, REQUEST_FILESELECT );
				return true;

		}
		return false;
	}

	// アクティビティ呼び出し結果の取得
	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent intent )
	{
		if( REQUEST_FILESELECT == requestCode && RESULT_OK == resultCode )
		{
			Bundle extras = intent.getExtras();
			if( null != extras )
			{
				Object[] aObject = (Object[])extras.getSerializable( FileSelectionActivity.EXTRA_FILE );
				if( null == aObject )
				{
					return;
				}
				StringBuilder sb = new StringBuilder();
				sb.append( "File Selected :\n" );
				for( Object object : aObject )
				{
					sb.append( ( (File)object ).getPath() );
					sb.append( "\n" );
				}
				Toast.makeText( this, sb.toString(), Toast.LENGTH_SHORT ).show();
				m_strInitialDir = ( (File)aObject[0] ).getParent();
			}
		}
	}

	// 初回表示時、および、ポーズからの復帰時
	@Override
	protected void onResume()
	{
		super.onResume();

		// 外部ストレージ読み込みパーミッション要求
		requestReadExternalStoragePermission();
	}

	// 外部ストレージ読み込みパーミッション要求
	private void requestReadExternalStoragePermission()
	{
		if( PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) )
		{    // パーミッションは付与されている
			return;
		}
		// パーミッションは付与されていない。
		// パーミッションリクエスト
		ActivityCompat.requestPermissions( this,
										   new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
										   REQUEST_PERMISSION_READ_EXTERNAL_STORAGE );
	}

	// パーミッション要求ダイアログの操作結果
	@Override
	public void onRequestPermissionsResult( int requestCode, String[] permissions, int[] grantResults )
	{
		switch( requestCode )
		{
			case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
				if( grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED )
				{
					// 許可されなかった場合
					Toast.makeText( this, "Permission denied.", Toast.LENGTH_SHORT ).show();
					finish();    // アプリ終了宣言
					return;
				}
				break;
			default:
				break;
		}
	}
}
