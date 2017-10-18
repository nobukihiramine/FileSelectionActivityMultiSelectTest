package com.hiramine.fileselectionactivitymultiselecttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

public class FileSelectionActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener
{
	static public class FileInfo implements Comparable<FileInfo>
	{
		private String  m_strName;    // 表示名
		private File    m_file;    // ファイルオブジェクト
		private boolean m_bSelected;    // 選択状態

		// コンストラクタ
		public FileInfo( String strName, File file )
		{
			m_strName = strName;
			m_file = file;
		}

		public String getName()
		{
			return m_strName;
		}

		public File getFile()
		{
			return m_file;
		}

		public boolean isSelected()
		{
			return m_bSelected;
		}

		public void setSelected( boolean bSelected )
		{
			m_bSelected = bSelected;
		}

		// 比較
		public int compareTo( FileInfo another )
		{
			// ディレクトリ < ファイル の順
			if( m_file.isDirectory() && !another.getFile().isDirectory() )
			{
				return -1;
			}
			if( !m_file.isDirectory() && another.getFile().isDirectory() )
			{
				return 1;
			}

			// ファイル同士、ディレクトリ同士の場合は、ファイル名（ディレクトリ名）の大文字小文字区別しない辞書順
			return m_file.getName().toLowerCase().compareTo( another.getFile().getName().toLowerCase() );
		}
	}

	static public class FileInfoArrayAdapter extends BaseAdapter
	{
		private Context        m_context;
		private List<FileInfo> m_listFileInfo; // ファイル情報リスト

		// コンストラクタ
		public FileInfoArrayAdapter( Context context, List<FileInfo> list )
		{
			super();
			m_context = context;
			m_listFileInfo = list;
		}

		@Override
		public int getCount()
		{
			return m_listFileInfo.size();
		}

		@Override
		public FileInfo getItem( int position )
		{
			return m_listFileInfo.get( position );
		}

		@Override
		public long getItemId( int position )
		{
			return position;
		}

		static class ViewHolder
		{
			TextView textviewFileName;
			TextView textviewFileSize;
		}

		// 一要素のビューの生成
		@Override
		public View getView( int position, View convertView, ViewGroup parent )
		{
			ViewHolder viewHolder;
			if( null == convertView )
			{
				// レイアウト
				LinearLayout layout = new LinearLayout( m_context );
				layout.setOrientation( LinearLayout.VERTICAL );
				layout.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );
				// ファイル名テキスト
				TextView textviewFileName = new TextView( m_context );
				textviewFileName.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 24 );
				layout.addView( textviewFileName, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );
				// ファイルサイズテキスト
				TextView textviewFileSize = new TextView( m_context );
				textviewFileSize.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 12 );
				layout.addView( textviewFileSize, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );

				convertView = layout;
				viewHolder = new ViewHolder();
				viewHolder.textviewFileName = textviewFileName;
				viewHolder.textviewFileSize = textviewFileSize;
				convertView.setTag( viewHolder );
			}
			else
			{
				viewHolder = (ViewHolder)convertView.getTag();
			}

			FileInfo fileinfo = m_listFileInfo.get( position );
			if( fileinfo.getFile().isDirectory() )
			{ // ディレクトリの場合は、名前の後ろに「/」を付ける
				viewHolder.textviewFileName.setText( fileinfo.getName() + "/" );
				viewHolder.textviewFileSize.setText( "(directory)" );
			}
			else
			{
				viewHolder.textviewFileName.setText( fileinfo.getName() );
				viewHolder.textviewFileSize.setText( String.valueOf( fileinfo.getFile().length() / 1024 ) + " [KB]" );
			}


			// 選択行と非選択行
			if( fileinfo.isSelected() )
			{ // 選択行：青背景、白文字
				convertView.setBackgroundColor( Color.BLUE );
				viewHolder.textviewFileName.setTextColor( Color.WHITE );
				viewHolder.textviewFileSize.setTextColor( Color.WHITE );
			}
			else
			{ // 非選択行：白背景、黒文字
				convertView.setBackgroundColor( Color.WHITE );
				viewHolder.textviewFileName.setTextColor( Color.BLACK );
				viewHolder.textviewFileSize.setTextColor( Color.BLACK );
			}

			return convertView;
		}

		// 一番上の選択されたアイテムの位置の取得
		public int getFirstSelectedItemPosition()
		{
			ListIterator<FileInfo> it = m_listFileInfo.listIterator();
			while( it.hasNext() )
			{
				FileInfo fileinfo = it.next();
				if( fileinfo.isSelected() )
				{
					return it.nextIndex() - 1;
				}
			}
			return -1;
		}

		// positionで指定したアイテムの次の選択されたアイテムの位置の取得
		public int getNextSelectedItemPosition( int iPosition )
		{
			ListIterator<FileInfo> it = m_listFileInfo.listIterator( iPosition );
			// 一つ進める
			it.next();
			while( it.hasNext() )
			{
				FileInfo fileinfo = it.next();
				if( fileinfo.isSelected() )
				{
					return it.nextIndex() - 1;
				}
			}
			return -1;
		}
	}

	// 定数
	public static final  String EXTRA_INITIAL_DIR = "INITIAL_DIR";
	public static final  String EXTRA_FILE        = "FILE";
	public static final  String EXTRA_EXT         = "EXT";
	private static final int    BUTTONTAG_CANCEL  = 0;
	private static final int    BUTTONTAG_OK      = 1;

	// メンバー変数
	private ListView             m_listview;    // リストビュー
	private FileInfoArrayAdapter m_fileinfoarrayadapter; // ファイル情報配列アダプタ
	private String[]             m_astrExt;                // フィルタ拡張子配列

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		// setContentView( R.layout.activity_file_selection );

		// アクティビティの戻り値の初期化
		setResult( Activity.RESULT_CANCELED );

		// 呼び出し元からパラメータ取得
		String strInitialDir = null;
		String strExt        = null;
		Bundle extras        = getIntent().getExtras();
		if( null != extras )
		{
			strInitialDir = extras.getString( EXTRA_INITIAL_DIR );
			strExt = extras.getString( EXTRA_EXT );
		}
		if( null == strInitialDir || !( new File( strInitialDir ).isDirectory() ) )
		{
			strInitialDir = "/";
		}

		// 拡張子フィルタ
		if( null != strExt )
		{
			StringTokenizer tokenizer   = new StringTokenizer( strExt, "; " );
			int             iCountToken = 0;
			while( tokenizer.hasMoreTokens() )
			{
				tokenizer.nextToken();
				iCountToken++;
			}
			if( 0 != iCountToken )
			{
				m_astrExt = new String[iCountToken];
				tokenizer = new StringTokenizer( strExt, "; " );
				iCountToken = 0;
				while( tokenizer.hasMoreTokens() )
				{
					m_astrExt[iCountToken] = tokenizer.nextToken();
					iCountToken++;
				}
			}
		}

		LinearLayout.LayoutParams layoutparams;
		// レイアウト
		LinearLayout layout = new LinearLayout( this );
		layoutparams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT );
		layout.setLayoutParams( layoutparams );
		layout.setOrientation( LinearLayout.VERTICAL );
		setContentView( layout );

		// リストビュー
		m_listview = new ListView( this );
		m_listview.setOnItemClickListener( this );
		layoutparams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, 0 );
		layoutparams.weight = 1;
		layout.addView( m_listview, layoutparams );

		// ボタン用レイアウト
		LinearLayout layoutButton = new LinearLayout( this );
		layoutButton.setOrientation( LinearLayout.HORIZONTAL );
		layoutparams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
		layoutButton.setLayoutParams( layoutparams );
		layout.addView( layoutButton );

		// ボタン
		Button button = new Button( this );
		button.setOnClickListener( this );
		button.setTag( BUTTONTAG_CANCEL );
		button.setText( "Cancel" );
		layoutparams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
		layoutparams.weight = 1;
		layoutButton.addView( button, layoutparams );

		button = new Button( this );
		button.setOnClickListener( this );
		button.setTag( BUTTONTAG_OK );
		button.setText( "OK" );
		layoutparams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
		layoutparams.weight = 1;
		layoutButton.addView( button, layoutparams );

		fill( new File( strInitialDir ) );
	}

	// アクティビティ内の表示内容構築
	private void fill( File fileDirectory )
	{
		// タイトル
		setTitle( fileDirectory.getAbsolutePath() );

		// ファイルリスト
		File[]         aFile        = fileDirectory.listFiles( getFileFilter() );
		List<FileInfo> listFileInfo = new ArrayList<>();
		if( null != aFile )
		{
			for( File fileTemp : aFile )
			{
				listFileInfo.add( new FileInfo( fileTemp.getName(), fileTemp ) );
			}
			Collections.sort( listFileInfo );
		}
		// 親フォルダに戻るパスの追加
		if( null != fileDirectory.getParent() )
		{
			listFileInfo.add( 0, new FileInfo( "..", new File( fileDirectory.getParent() ) ) );
		}

		m_fileinfoarrayadapter = new FileInfoArrayAdapter( this, listFileInfo );
		m_listview.setAdapter( m_fileinfoarrayadapter );
	}

	@Override
	public void onClick( View v )
	{
		switch( (int)v.getTag() )
		{
			case BUTTONTAG_CANCEL:
				// アクティビティ終了
				finish();
				break;
			case BUTTONTAG_OK:
				// ファイルが選択されているか
				int iPosition_first = m_fileinfoarrayadapter.getFirstSelectedItemPosition();
				int iPosition = iPosition_first;
				int iCount = 0;
				while( -1 != iPosition )
				{
					iPosition = m_fileinfoarrayadapter.getNextSelectedItemPosition( iPosition );
					iCount++;
				}
				if( 0 == iCount )
				{ // ひとつも選択されていない
					Toast.makeText( this, "No file selected.", Toast.LENGTH_SHORT ).show();
				}
				else
				{ // ひとつ以上選択されている
					File[] aFile = new File[iCount];
					iPosition = iPosition_first;
					iCount = 0;
					while( -1 != iPosition )
					{
						aFile[iCount] = m_fileinfoarrayadapter.getItem( iPosition ).getFile();
						iPosition = m_fileinfoarrayadapter.getNextSelectedItemPosition( iPosition );
						iCount++;
					}

					// 呼び出し元へのパラメータ設定
					Intent intent = new Intent();
					intent.putExtra( EXTRA_FILE, aFile );
					// アクティビティの戻り値の設定
					setResult( Activity.RESULT_OK, intent );

					// アクティビティ終了
					finish();
				}
				break;
		}
	}

	@Override
	public void onItemClick( AdapterView<?> parent, View view, int position, long id )
	{
		FileInfo fileinfo = m_fileinfoarrayadapter.getItem( position );

		if( fileinfo.getFile().isDirectory() )
		{
			fill( fileinfo.getFile() );
		}
		else
		{ // ファイルをクリックしたときは、選択状態の反転と、クリック行の表示更新
			fileinfo.setSelected( !fileinfo.isSelected() );
			parent.getAdapter().getView( position, view, parent );
		}
	}

	// FileFilterオブジェクトの生成
	private FileFilter getFileFilter()
	{
		return new FileFilter()
		{
			public boolean accept( File file )
			{
				if( null == m_astrExt )
				{ // フィルタしない
					return true;
				}
				if( file.isDirectory() )
				{ // ディレクトリのときは、true
					return true;
				}
				for( String strTemp : m_astrExt )
				{
					if( file.getName().toLowerCase().endsWith( "." + strTemp ) )
					{
						return true;
					}
				}
				return false;
			}
		};
	}
}

