package data.strategy.user.s13t208;

import sys.game.GameBoard;
import sys.game.GameCompSub;
import sys.game.GameHand;
import sys.game.GamePlayer;
import sys.game.GameState;
import sys.struct.GogoHand;
import sys.user.GogoCompSub;

public class User_s13t208_00 extends GogoCompSub {
  
//====================================================================
//  コンストラクタ
//====================================================================

  public User_s13t208_00(GamePlayer player) {
    super(player);
    name = "s13t208";    // プログラマが入力
    
  }

//--------------------------------------------------------------------
//  コンピュータの着手
//--------------------------------------------------------------------

  public synchronized GameHand calc_hand(GameState state, GameHand hand) {
    theState = state;
    theBoard = state.board;
    lastHand = hand;
    
    //--  置石チェック
    init_values(theState, theBoard);
    
    //--  評価値の計算
    calc_values(theState, theBoard);
    // 先手後手、取石数、手数(序盤・中盤・終盤)で評価関数を変える
    
    //--  着手の決定
    return deside_hand();
    
  }

//----------------------------------------------------------------
//  置石チェック
//----------------------------------------------------------------

  public void init_values(GameState prev, GameBoard board) {
    this.size = board.SX;
    values = new int[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (board.get_cell(i, j) != board.SPACE) {
          values[i][j] = -2;
        } else {
          if (values[i][j] == -2) {
            values[i][j] = 0;
          }
        }
      }
    }
  }

//----------------------------------------------------------------
//  評価値の計算
//----------------------------------------------------------------

  public void calc_values(GameState prev, GameBoard board) {
    int [][] cell = board.get_cell_all();  // 盤面情報
    int mycolor;                  // 自分の石の色
    mycolor = role;
//追加   
    int mystone = get_mystone( prev );
    int enemystone =  get_enemystone( prev );
//    
    
    //--  各マスの評価値

    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        
        // 埋まっているマスはスルー
        if (values[i][j] == -2) { continue; }
        //--  適当な評価の例
        // 相手の五連を崩す → 1000;
        /*
          禁じ手の判定
        */
       
       //----------------------------------------------------------------
       if ( check_taboo( cell, mycolor, i, j )  ) {
          values[i][j] = 1;
          continue;
        }
       //----------------------------------------------------------------
       
       // 勝利(五取) → 950;
          if ( mystone == 8 && check_rem(cell, mycolor, i, j) ) {
            values[i][j] = 950;
            continue;
        }
        //勝利：飛び四(五連を作る)
	        if ( check_jump4( cell, mycolor, i, j) )  {
	          values[i][j] = 950;
            continue;
	        }
        
        //勝利(五連) → 900;
        if ( check_run(cell, mycolor, i, j, 5) ) {
          values[i][j] = 900;
          continue;
      }
        // 敗北阻止(五取) → 850;
        if ( enemystone == 8 && check_rem(cell, mycolor*-1, i, j) ) {
            values[i][j] = 850;
            continue;
        }
        // 敗北阻止(五連) → 800;
        if ( check_run(cell, mycolor*-1, i, j, 5) ) {
          values[i][j] = 800;
          continue;
        }
        //相手の飛び四(五連阻止)
	      if ( check_jump4( cell, mycolor*-1, i, j) ) {
          values[i][j] = 800;
          continue;
	      }
        
        // 相手の四連を止める → 700;
        if ( check_run(cell, mycolor*-1, i, j, 4) ) {
          int k = 0;
          k =check_edge(cell, mycolor*-1, i, j, 4); 
          values[i][j] = 700;
           switch ( k ) {
            case 2: break;
            case 1:
            case 0: values[i][j] = 1; break;
          }

          continue;
        }
        
        //相手の飛び三(四連阻止)
	      if ( check_jump3( cell, mycolor*-1, i, j) ) {
          int k = check_edge_tobi3(cell, mycolor*-1, i, j);
	        values[i][j] = 700;
	        
          switch ( k ) {
            case 2: values[i][j] = values[i][j] + 10; break;
            case 1: 
            case 0: values[i][j] = 1; break;
          }
          
          if ( k != 0 ) { continue; }
	      }
        
        //相手の石を取る 8個目
        if ( check_rem(cell, mycolor*-1, i, j) && mystone == 6 ) { values[i][j] = 600; continue; }
        
        // 自分の四連を作る → 600;
        if ( check_run(cell, mycolor, i, j, 4) ) {
          int k = check_edge( cell, mycolor, i, j, 4);
          
          values[i][j] = 600;
          
          switch ( k ) {
            case 2: break;
            case 1:
            case 0: values[i][j] = 1; break;
          }
          
          if ( k != 0 ) { continue; }
        }
        //飛び三(四連を作る)
	      if ( check_jump3( cell, mycolor, i, j) )  {
	        int k = check_edge_tobi3(cell, mycolor, i, j);
	        values[i][j] = 600;
	        
	        switch ( k ) {
            case 2: values[i][j] += 10; break;
            case 1: break;
            case 0: values[i][j] = 1; break;
          }
          if ( k != 0 ) { continue; }
	        
	      }
        
        //○●●の並びになる形を作る(k= 3,2)
        if ( exist_take( cell, mycolor, i, j) ) {
          int k = form_take(cell, mycolor, i, j);
          switch ( k ) {
            case 3: values[i][j] = 600; break;
            case 2: values[i][j] = 580; break;
            case 1: break;
          }
          if ( values[i][j] != 0 ) { continue; }
        }
        
        // 自分の石を守る → 550;
        if ( check_rem(cell, mycolor, i, j) ) { values[i][j] = 550; continue; }
         
	      // 相手の石を取る → http://screenx.tv/joniy520;
        if ( check_rem(cell, mycolor*-1, i, j) ) { values[i][j] = 520; continue; }
        
        //○●●の並びになる形を作る(k= 3,2)
        if ( exist_take_2( cell, mycolor, i, j) ) {
          values[i][j] = 1; continue;
        }
        
        // 相手の三連を防ぐ → 500;
        if ( check_run(cell, mycolor*-1, i, j, 3) ) {
          values[i][j] = 500;
          continue;
        }
         
        //自分の石が取られる形にしない
        if ( taken_form( cell, mycolor, i, j  ) ) {
          values[i][j] = 2;
          continue;
        }
        //五連を作れず、端にあたる
        if ( no_win ( cell, mycolor, i, j ) ) {
          values[i][j] = 3;
        }

        // 自分の三連を作る → 400;
        if ( check_run(cell, mycolor, i, j, 3) ) { 
          int k = check_edge( cell, mycolor, i, j, 3); 
          
          values[i][j] = 400;
          
          switch ( k ) {
            case 2: values[i][j] += 10; break;
            case 1: values[i][j] = 1; break;
            case 0: values[i][j] = 1; break;
          }
          if ( k != 0 ) { continue; }
          
        }
        
        //飛び二(3連を作る)
        if ( check_jump2( cell, mycolor, i, j) )  {
	        int k = check_edge_tobi2(cell, mycolor, i, j);
	        values[i][j] = 400;
	        
	        switch ( k ) {
            case 2: values[i][j] += 10; break;
            case 1: 
            case 0: values[i][j] = 1; break;
          }
          if ( k != 0 ) { continue; }
	        
	      }
        
        //○●●の並びになる形を作る( 一つだけの時 )
        if ( exist_take( cell, mycolor, i, j) ) {
          values[i][j] = 200;
        }
	
        //二連の評価
        if ( check_run(cell, mycolor, i, j, 2) ) { 
          int k = check_ren2(cell, mycolor, i, j);
          values[i][j] = 100;
          
          switch ( k ) {
            case 2: values[i][j] += 50; break;
            case 1: values[i][j] = 1; break;
            case 0: values[i][j] = 1; break;
          }
          if ( k != 0 ) { continue; }
          
        }      
        
//序盤の組み立て
        if ( prev.step < 8 ) {
          if ( i < 3 || i > 9 || j < 3 || j > 9 ) {
	          values[i][j] = 2; 
	        }
        }
        
        // ランダム
        if (values[i][j] == 0) {
          int aaa = (int) Math.round(Math.random() * 15);
          if (values[i][j] < aaa) { values[i][j] = aaa; }
        }
        // 四々や四三の判定
        // 飛び三や飛び四の判定
        // 三をどちらで止めるか
      }
    }

//----------------------------------------------------------------
//  評価値チェック&手の場所
//----------------------------------------------------------------
    for (int i = 0; i < size; i++) {
        System.out.print("{");
      for (int j = 0; j < size; j++) {
        System.out.printf("%3d ",values[j][i]);
      }
      System.out.println("}");
    }
    System.out.println("");
//-----------------------------------------------------------------

  }

//----------------------------------------------------------------
//  連の全周チェック
//----------------------------------------------------------------

  boolean check_run(int[][] board, int color, int i, int j, int len) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_run_dir(board, color, i, j, dx, dy, len) ) { return true; }
      }
    }
    return false;
  }

//----------------------------------------------------------------
//  連の方向チェック(止連・端連・長連も含む、飛びは無視)
//----------------------------------------------------------------
  
  boolean check_run_dir(int[][] board, int color, int i, int j, int dx, int dy, int len) {
    for ( int k = 1; k < len; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
      if ( board[i+k*dx][j+k*dy] != color ) { return false; }
    }
    return true;
  }

//----------------------------------------------------------------
//  取の全周チェック(ダブルの判定は無し)
//----------------------------------------------------------------
  
  boolean check_rem(int [][] board, int color, int i, int j) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_rem_dir(board, color, i, j, dx, dy) ) { return true; }
      }
    }
    return false;
  }

//----------------------------------------------------------------
//  取の方向チェック
//----------------------------------------------------------------
    
  boolean check_rem_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    int len = 3;
    for ( int k = 1; k <= len; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
      if ( board[i+k*dx][j+k*dy] != color ) { return false; }
      if (k == len-1) { color *= -1; }
    }
    return true;
  }
//----------------------------------------------------------------
//  着手の決定
//----------------------------------------------------------------

  public GameHand deside_hand() {
    GogoHand hand = new GogoHand();
    hand.set_hand(0, 0);  // 左上をデフォルトのマスとする
    int value = -1;       // 評価値のデフォルト
    //int max_i = 0, max_j = 0;
    //--  評価値が最大となるマス
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (value < values[i][j]) {
          hand.set_hand(i, j);
          value = values[i][j];
          /*max_i = i;
          max_j = j;*/
        }
      }
    }
    //System.out.println("i:"+max_i+",j:"+max_j);
    return hand;
  }

//----------------------------------------------------------------
//  2連の評価
//----------------------------------------------------------------
  
  int check_ren2(int[][] board, int color, int i, int j) {
    int k = 0;
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_run_dir(board, color, i, j, dx, dy, 2) ) { 
          k = check_edge_dir( board, color, i, j, dx, dy, 2);
        }
        
      }
    }
    return k;
  }
  
/*
  飛びチェック
*/
//----------------------------------------------------------------
//  飛び四のチェック
//----------------------------------------------------------------

  boolean check_jump4(int[][] board, int color, int i, int j) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
          
	      if ( check_run_dir(board, color, i, j, dx, dy, 3)  && check_run_dir(board, color, i, j, -dx, -dy, 3) ) { 
	        return true;
	      }
        
    	  if ( check_run_dir(board, color, i, j, dx, dy, 4)  && check_run_dir(board, color, i, j, -dx, -dy, 2) ) { 
	        return true;
	      }

      }
    }
    return false;
  }
  
//----------------------------------------------------------------
//  飛び三のチェック
//----------------------------------------------------------------

  boolean check_jump3(int[][] board, int color, int i, int j) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
          
	if ( check_run_dir(board, color, i, j, dx, dy, 2)  && check_run_dir(board, color, i, j, -dx, -dy, 3) ) { 
	  return true;
	}
 
      }
    }
    return false;
  }
  
 //----------------------------------------------------------------
//  飛び二のチェック
//----------------------------------------------------------------

  boolean check_jump2(int[][] board, int color, int i, int j) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
          
	      if ( check_run_dir(board, color, i, j, dx, dy, 2)  && check_run_dir(board, color, i, j, -dx, -dy, 2) ) { 
	        return true;
      	}
        
	    }
    }
    return false;
  }
  
/*
  連の判定（飛び石なし）
*/
//----------------------------------------------------------------
//  lenの両端の判定(飛び石は無視)
//  両端が空いている場合+10,片方なら1,空いていないなら0を評価値に掛ける
//----------------------------------------------------------------
    int check_edge(int[][] board, int color, int i, int j, int len) {
      
    int k = 0;
      
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        k = check_edge_dir(board, color, i,  j, dx, dy, len);
        if ( k !=  0) { return k; }
      }
    }
    
    return 0;
  }  
//----------------------------------------------------------------
//  連の方向チェック&&連の両端の色の確認
//----------------------------------------------------------------
  
  int check_edge_dir(int[][] board, int color, int i, int j, int dx, int dy, int len) {
    int edge = 0;
    int x, y;
    int k;
    
    //連の端が空所なら＋
    if ( i+(dx*-1) >= 0 && j+(dy*-1) >= 0 && i+(dx*-1) < size && j+(dy*-1) < size) {
      if( board[i+(dx*-1)][j+(dy*-1)] == 0 ) { edge++; }
    }
    for ( k = 1; k < len; k++ ) {
      x = i+k*dx;
      y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return 0; }
      if ( board[i+k*dx][j+k*dy] != color ) { return 0; }
      
    }
    
    x = i+k*dx;
    y = j+k*dy;
    
    if( x >= 0 && y >= 0 && x < size && y < size ) {
      if ( board[x][y] == 0 ) {
        edge++;
      }
    }
    
    return edge;
  }
  
/*
  飛び連の時の両端
*/

//----------------------------------------------------------------
//  飛び三の両端のチェック
//----------------------------------------------------------------

  int check_edge_tobi3(int[][] board, int color, int i, int j) {
    int k = 0;
    
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
          
	      if ( check_run_dir(board, color, i, j, dx, dy, 2)  && check_run_dir(board, color, i, j, -dx, -dy, 3) ) { 
	        k += check_edge_tobi( board, color, i, j, dx, dy, 2);
	        k += check_edge_tobi( board, color, i, j, -dx, -dy, 3);
	        return k;
	      }
      
      }
    }
    return 0;
  }
//----------------------------------------------------------------
//  飛び二の両端のチェック
//----------------------------------------------------------------

  int check_edge_tobi2(int[][] board, int color, int i, int j) {
    int k = 0;
    
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
          
	if ( check_run_dir(board, color, i, j, dx, dy, 2)  && check_run_dir(board, color, i, j, -dx, -dy, 2) ) { 
	  k += check_edge_tobi( board, color, i, j, dx, dy, 2);
	  k += check_edge_tobi( board, color, i, j, -dx, -dy, 2);
	  return k;
	}

      }
    }
    return 0;
  }

//----------------------------------------------------------------
//  連の方向チェック&&連の両端の色の確認
//----------------------------------------------------------------
  
  int check_edge_tobi(int[][] board, int color, int i, int j, int dx, int dy, int len) {
    int edge = 0;
    int x, y;
    int k;
    
    for ( k = 1; k < len; k++ ) {
      x = i+k*dx;
      y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return 0; }
      if ( board[i+k*dx][j+k*dy] != color ) { return 0; }
    }
    
    x = i+k*dx;
    y = j+k*dy;
    
  if( x >= 0 && y >= 0 && x < size && y < size ) {
    if ( board[x][y] == 0 ) {
      edge++;
    }
  }
    
    return edge;
 }
//------------------------------------
//  ○●●の並びを作る
//------------------------------------
//全周チェック
  int form_take(int[][] board, int color, int i, int j) {
    int k = 0;
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( form_take_dir(board, color, i, j, dx, dy) ) { k++; }
      }
    }
    
    return k;
  }
//その形が存在するかどうか
  boolean exist_take(int[][] board, int color, int i, int j) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( form_take_dir(board, color, i, j, dx, dy) ) { return true; }
      }
    }
    return false;
  }
// 連の方向チェック
  
  boolean form_take_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    int k;
    for ( k = 1; k < 3; k++ ) {
      int x = i+k*dx;
      int y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { return false; }
      if ( board[i+k*dx][j+k*dy] != color*-1 ) { return false; }
    }
    int x = i+k*dx;
    int y = j+k*dy;
    if ( !check_out(x, y) && board[x][y] != 0 ) { return false; }
    
    return true;
  }
 
//----------------------------------------------
//  ○●●の並びを防ぐ( ○  ●のとき )
//----------------------------------------------
//その形が存在するかどうか
  boolean exist_take_2(int[][] board, int color, int i, int j) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( form_take_dir(board, color, i, j, dx, dy) ) { return true; }
      }
    }
    return false;
  }
// その方向と反対方向のチェック
  boolean take_2_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    int k = 0;
    int x = i+dx;
    int y = j+dy;
    
    if ( x < 0 || y < 0 || x >= size || y >= size ) {
      if ( board[i+k*dx][j+k*dy] != 0 ) { return false; }
    }
     x = i+dx*-1;
     y = j+dy*-1;
    if ( !check_out(x, y) && board[x][y] != color ) { return false; }
    
    return true;
  }

//----------------------------------------------------------------
//   もし五連を揃えられないなら、その方向に置かない
//----------------------------------------------------------------  
  boolean no_win( int[][] board, int color, int i, int j )
  {
   int k = 0;
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( no_win_dir(board, color, i, j, dx, dy) ) { return true; }
      }
    }
    
    return false;

  }

  boolean no_win_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    //三連の時(●○○)
    if ( !check_out(i+dx, j+dy) && board[i+dx][j+dy] == color && !check_out(i+dx*2, j+dy*2) && board[i+dx*2][j+dy*2] == color && !check_out(i+dx*3, j+dy*3) && board[i+dx*3][j+dy*3] == color*-1 ) {
      if ( check_out(i-dx, j-dy) || check_out(i-dx*2, j-dy*2) ) {
        return true;
      }
    }

     //三連の時（ ●  ○○ || ●○  ○ ）
    if (  !check_out(i+dx, j+dy) && board[i+dx][j+dy] == color && !check_out(i+dx*2, j+dy*2) && board[i+dx*2][j+dy*2] == color && !check_out(i-dx, j-dy) && board[i-dx][j-dy] == color*-1 ) {
      if ( check_out(i+dx*3, j+dy*3) || check_out(i+dx*4, j+dy*4) ) {
        return true; 
      }
    }  
    if (  !check_out(i-dx*2, j-dy*2) && board[i-dx*2][j-dy*2] == color*-1 && !check_out(i-dx, j-dy) && board[i-dx][j-dy] == color && !check_out(i+dx, j+dy) && board[i+dx][j+dy] == color ) {
      if ( check_out(i+dx*2, j+dy*2) || check_out(i+dx*3, j+dy*3) ) {
        return true; 
      }
    } 
    return false;
  }

//----------------------------------------------------------------
// 取られる型にしない
//----------------------------------------------------------------
  boolean taken_form( int[][] board, int color, int i, int j )
  {
   int k = 0;
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( taken_form_dir(board, color, i, j, dx, dy) ) { return true; }
      }
    }
    
    return false;

  }

  boolean taken_form_dir(int[][] board, int color, int i, int j, int dx, int dy) {
    //●○○にしない（ ●  ○ ）
    if ( !check_out(i+dx, j+dy) && board[i+dx][j+dy] == color && !check_out(i+dx*2, j+dy*2) && board[i+dx*2][j+dy*2] == color ) {
      if ( !check_out(i-dx, j-dy) && board[i-dx][j-dy] == color && !check_out(i-dx*2, j-dy*2) && board[i-dx*2][j-dy*2] == color*-1 ) {
        return true;
      }
    }

     //●○○にしない（ ●○ ）
    if (  !check_out(i-dx, j-dy) && board[i-dx][j-dy] == 0 && !check_out(i+dx, j+dy) && board[i+dx][j+dy] == color && !check_out(i+dx*2, j+dy*2) && board[i+dx*2][j+dy*2] == color*-1 && !check_out(i+dx*3, j+dy*3) && board[i+dx*3][j+dy*3] == 0 ) {
      return true; 
    }  
    
    return false;
  }

//----------------------------------------------------------------
//  範囲外かどうかのチェック
//----------------------------------------------------------------  
  boolean check_out(int x, int y)
  {
    return (x < 0 || y < 0 || x >= size || y >= size);
  }

//----------------------------------------------------------------
//  禁じ手のチェック
//----------------------------------------------------------------
boolean check_taboo(int[][] board, int color, int i, int j ) {
   int k = 0;
   int tmp = 0;
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        //○○  ○○ のパターンの排斥
        if ( form_take_dir(board, color*-1, i, j, dx, dy) && form_take_dir(board, color*-1, i, j, dx*-1, dy*-1) ) { return false; }
         
        if ( taboo( board, color, i, j, dx, dy) ) { return true; }
      }
    }

    if ( taboo_tobi2( board, color, i, j ) ) { return true; } 

    return false;
}
//禁じ手
 boolean taboo(int[][] board, int color, int i, int j, int dx, int dy) 
 {
    int k = 0;
    //○○
    if ( !check_out(i+dx, j+dy) && !check_out( i+dx*2,  j+dy*2 )  && !check_out( i+dx*3,  j+dy*3 ) ) {
      if ( board[i+dx][j+dy] == color && board[i+dx*2][j+dy*2] == color && board[i+dx*3][j+dy*3] == 0 ) {
        k = form_take( board, color*-1, i, j); 
        if ( k == 2 ) { return true; }
        
        if ( exist_take( board, color*-1, i+dx, j+dy) ) { return true; }

        if ( exist_take( board, color*-1, i+dx*2, j+dy*2) ) { return true; }
      }
    }
    //○  ○
    if ( !check_out( i+dx, j+dy ) && !check_out( i+dx*2, j+dy*2 ) && !check_out( i-dx, j-dx ) && !check_out( i-dx*2, j-dy*2 ) ) {
      if ( board[i+dx][j+dy] == color && board[i+dx*2][j+dy*2] == 0 && board[i-dx][j-dy]== color && board[i-dx*2][j-dy*2] == 0 ) {
        if ( exist_take( board, color*-1, i, j) ) { return true; }
        if ( exist_take( board, color*-1, i+dx, j+dy) ) { return true; }
        if ( exist_take( board, color*-1, i-dx, j-dy) ) { return true; }
       
      }
    }
       return false;
  }
  
  boolean taboo_tobi2(int[][] board, int color, int i, int j ) 
  {
    //  ○
    //○  ○ の判定
    //  ○
    int k = 0;
     for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( !check_out(i+dx, j+dy) && !check_out( i+dx*2,  j+dy*2 )  && !check_out( i-dx,  j-dy ) && !check_out( i-dx*2,  j-dy*2 ) ) {
          if ( check_run_dir(board, color, i, j, dx, dy, 2)  && check_run_dir(board, color, i, j, -dx, -dy, 2) && board[i+dx*2][j+dy*2] == 0 && board[i-dx*2][j-dy*2] == 0 ) { k++; }
        }
      }
     }
     if ( k == 4 ) { return true; }

    return false;
  }

//--------------------------------------------------------------------
//クラスの}
//--------------------------------------------------------------------
}
