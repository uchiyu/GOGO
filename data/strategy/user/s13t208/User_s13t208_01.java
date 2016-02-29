package data.strategy.user.s13t208;

import sys.game.GameBoard;
import sys.game.GameCompSub;
import sys.game.GameHand;
import sys.game.GamePlayer;
import sys.game.GameState;
import sys.struct.GogoHand;
import sys.user.GogoCompSub;

public class User_s13t208_01 extends GogoCompSub {
  public static int decreasePoint = 0;
  //====================================================================
  //  コンストラクタ
  //====================================================================

  public User_s13t208_01(GamePlayer player) {
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
          values[i][j] = -10;
        } else {
          if (values[i][j] == -10) {
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

    //--  各マスの評価値
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        // 埋まっているマスはスルー
        if (values[i][j] < 0) { continue; }
        //--  適当な評価の例
        // 禁じ手の判定
        if ( check_taboo(cell, mycolor, i, j) ) {
          values[i][j] = -50;
          continue;
        }

        // 相手の五連を崩す → 1000; 
        // 実装してもあんま意味ない

        // 勝利(五取) → 950;
        if ( mystone == 8 && check_rem(cell, mycolor*-1, i, j) ) {
          values[i][j] = 950;
          continue;
        }
        //勝利：飛び四(五連を作る)
        if ( check_jump( cell, mycolor, i, j, 5) )  {
          values[i][j] = 950;
          continue;
        }
        // 勝利(五連) → 900;
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
          values[i][j] -= decreasePoint;
          continue;
        }
        //相手の飛び四(五連阻止)
        if ( check_jump( cell, mycolor*-1, i, j, 5) ) {
          values[i][j] = 800;
          continue;
        }

        // 相手の四連を止める → 700;
        if ( check_run(cell, mycolor*-1, i, j, 4) ) {
          values[i][j] = 500;
          int k = checkEdge(cell, mycolor*-1, i, j, 4);
          switch ( k ) {
            case 2: values[i][j] += 200; break;
            case 1: values[i][j] = 10; break;
            case 0: values[i][j] = 1; break;
          }
          if ( taken_form( cell, mycolor, i, j ) ) { values[i][j] -= 20; }
          values[i][j] -= decreasePoint;
          if ( k == 2 ) { continue; }
        }

        //相手の飛び三(四連阻止)
        if ( check_jump( cell, mycolor*-1, i, j, 4) ) {
          int k = checkJumpEdge(cell, mycolor*-1, i, j, 4);
          values[i][j] = 500;
          switch ( k ) {
            case 2: values[i][j] += 200; break;
            case 1: if ( check_rem(cell, mycolor*-1, i, j) ) { values[i][j] = 10; } break;
            case 0: values[i][j] = 1; break;
          }
          if ( taken_form( cell, mycolor, i, j ) ) { values[i][j] -= 20; }
          if ( k == 2 ) { continue; }
        }

        //相手の石を取る 8個目
        if ( check_rem(cell, mycolor*-1, i, j) && mystone == 6 ) { values[i][j] = 600; continue; }

        // 自分の四連を作る → 600;
        if ( check_run(cell, mycolor, i, j, 4) ) {
          int k = checkEdge( cell, mycolor, i, j, 4);
          values[i][j] = 500;
          switch ( k ) {
            case 2: values[i][j] += 100; break;
            case 1: break;
            case 0: values[i][j] = 1; break;
          }
          values[i][j] -= decreasePoint;
          if ( k != 0 ) { continue; }
        }

        //飛び三(四連を作る)
        if ( check_jump( cell, mycolor, i, j, 4) )  {
          int k = checkJumpEdge( cell, mycolor, i, j, 4);
          values[i][j] = 500;

          switch ( k ) {
            case 2: values[i][j] += 100; break;
            case 1: break;
            case 0: values[i][j] = 1; break;
          }
          if ( k != 0 ) { continue; }
        }

        // 自分の石を守る → 550;
        if ( check_rem(cell, mycolor, i, j) ) { values[i][j] = 550; continue; }

        // 相手の石を取る → 520;
        if ( check_rem(cell, mycolor*-1, i, j) ) { values[i][j] = 520; continue; }

        //○●●の並びになる形を作る(k= 3,2)
        if ( exist_take( cell, mycolor, i, j) ) {
          int k = form_take(cell, mycolor, i, j);
          switch ( k ) {
            case 3: values[i][j] = 510; break;
            case 2: values[i][j] = 500; break;
            case 1: break;
          }
          if ( values[i][j] != 0 ) { continue; }
        }

        // 相手の三連を防ぐ → 580;
        if ( check_run(cell, mycolor*-1, i, j, 3) ) {
          values[i][j] = 480;
          values[i][j] -= decreasePoint;
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
          int k = checkEdge( cell, mycolor, i, j, 3);

          values[i][j] = 400;

          switch ( k ) {
            case 2: values[i][j] += 10; break;
            case 1: values[i][j] = 1; break;
            case 0: values[i][j] = 1; break;
          }
          values[i][j] -= decreasePoint;
          if ( k != 0 ) { continue; }

        }

        //飛び二(3連を作る)
        if ( check_jump( cell, mycolor, i, j, 3) )  {
          int k = checkJumpEdge(cell, mycolor, i, j, 3);
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
            case 1:
            case 0: values[i][j] = 1; break;
          }
          if ( k == 2 ) { continue; }
        }

        //序盤の組み立て
        //if ( prev.step < 8 ) {
        //  if ( i < 3 || i > 9 || j < 3 || j > 9 ) {
        //    values[i][j] = 2;
        //  }
        //}

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
    centerValueUp(values);

    //----------------------------------------------------------------
    //  評価値チェック&手の場所
    //----------------------------------------------------------------
    //for (int i = 0; i < size; i++) {
    //  System.out.print("{");
    //  for (int j = 0; j < size; j++) {
    //    System.out.printf("%3d ",values[j][i]);
    //    //System.out.printf("%3d ", cell[j][i]);
    //  }
    //  System.out.println("}");
    //}
    //System.out.println("");
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
    
    decreasePoint = 0;
    if ( len < 3 ) { return true; }
    // ５連が作れない場合は減点
    int num = 5 - len;
    for ( int k = 1; k <= num; k++ ) {
      int x = i+k*-dx;
      int y = j+k*-dy;
      if ( check_out(x, y) ) { decreasePoint += 10; }
      else if ( board[x][y] != 0 ) { decreasePoint += 5; }

      x = i+(len+k)*dx;
      y = j+(len+k)*dy;
      if ( check_out(x, y) ) { decreasePoint += 10; }
      else if ( board[x][y] != 0 ) { decreasePoint += 5; }
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
    //--  評価値が最大となるマス
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (value < values[i][j]) {
          hand.set_hand(i, j);
          value = values[i][j];
        }
      }
    }
    return hand;
  }

  //-------------------------------------------------------------------------
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
        if ( taboo( board, color, i, j, dx, dy) ) { return true; }
        if ( taboo_T( board, color, i, j, dx, dy) ) { return true; }
      }
    }

    if ( taboo_cross( board, color, i, j ) ) { return true; }

    return false;
  }
  //禁じ手
  boolean taboo(int[][] board, int color, int i, int j, int dx1, int dy1) 
  {
    //    ○
    //    ○
    //○○
    if ( !check_out(i-dy1, j-dx1) ) {
      if ( board[i-dy1][j-dx1] != 0 ) {
        return false;
      }
    }

    for ( int dx2 = -1; dx2 <= 1; dx2++ ) {
      for ( int dy2 = -1; dy2 <= 1; dy2++ ) {
        if ( dx2 == 0 && dy2 == 0 ) { continue; }
        // 同じ方向を除外
        if ( dx2 == dx1 && dy2 == dy1 ) { continue; }
        //○○  ○○ のパターンの排斥
        if ( -dx2 == dx1 && -dy2 == dy1 ) { continue; }
        if ( check_out(i+dy1*3, j+dx1*3) || check_out(i+dy2*3, j+dx2*3) ) { continue; }
        if ( !check_out(i-dy2, j-dx2) ) {
          if ( board[i-dy2][j-dx2] != 0 ) {
            continue;
          }
        }
        if ( taboo_len( board, color, i, j, dx1, dy1 ) && taboo_len( board, color, i, j, dx2, dy2 )) {
          return true;
        }
      }
    }
    return false;
  }

  boolean taboo_T(int[][] board, int color, int i, int j, int dx1, int dy1) 
  {
    // 先にニ連を検出し,そのあと両端
    //  ○
    //  ○
    //○  ○
    if ( ! taboo_len(board, color, i, j, dx1, dy1 ) ) { return false; }

    if ( !check_out(i-dy1, j-dx1) ) {
      if ( board[i-dy1][j-dx1] != 0 ) {
        return false;
      }
    }

    for ( int dx2 = -1; dx2 <= 1; dx2++ ) {
      for ( int dy2 = -1; dy2 <= 1; dy2++ ) {
        if ( dx2 == 0 && dy2 == 0 ) { continue; }
        // 同じ方向を除外
        if ( dx2 == dx1 && dy2 == dy1 ) { continue; }
        // 反対側の方向も除外
        if ( dx2 == -dx1 && dy2 == -dy1 ) { continue; }
        if ( check_out(j+dx1, i+dy1) || check_out(j-dx2, i-dy2) ) { continue; }
        if ( check_out(j+dx1*2, i+dy1*2) || check_out(j+dx2*2, i+dy2*2) ) { continue; }
        if ( board[i+dy2][j+dx2] != color || board[i-dy2][j-dx2] != color ) { continue; }
        if ( board[i+dy2*2][j+dx2*2] != 0 || board[i-dy2*2][j-dx2*2] != 0 ) { continue; }
        return true;
      }
    }
    return false;
  }

  // ◯◯空 という並び
  boolean taboo_len(int[][] board, int color, int i, int j, int dx, int dy)
  {
    int k = 1;
    for (k  = 1; k <= 2; k++ ) {
      if ( !check_out( j+dx*k, i+dy*k ) ) {
        if ( board[i+dy*k][j+dx*k] != color ) { return false; }
      }
    }
    if ( !check_out( j+dx*k, i+dy*k ) ) {
      if ( board[i+dy*k][j+dx*k] != 0 ) { return false; }
    }
    return true;
  }

  boolean taboo_cross(int[][] board, int color, int i, int j ) 
  {
    //  ○
    //○  ○ 関連のチェック
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

  //----------------------------------------------------------------
  //  飛びのチェック
  //----------------------------------------------------------------

  boolean check_jump(int[][] board, int color, int i, int j, int len) {
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        for ( int len1 = 2; len1 <= (len+1)/2; len1++ ) {
          int len2 = ( len + 1 ) - len1;
          if ( check_run_dir(board, color, i, j, dx, dy, len1)  && check_run_dir(board, color, i, j, -dx, -dy, len2) ) {
            return true;
          }
        }
      }
    }
    return false;
  }

  //----------------------------------------------------------------
  //  lenの両端の判定(飛び石は無視)
  //  両端が空いている場合+10,片方なら+5,空いていないなら0を評価値に掛ける
  //----------------------------------------------------------------
  int checkEdge(int[][] board, int color, int i, int j, int len) {

    int edgeSpaceNum = 0;

    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        edgeSpaceNum = checkEdgeDir(board, color, i,  j, dx, dy, len);
        if ( edgeSpaceNum !=  0) { return edgeSpaceNum; }
      }
    }

    return 0;
  }

  //----------------------------------------------------------------
  //  連の両端の確認(飛びは別)
  //----------------------------------------------------------------

  int checkEdgeDir(int[][] board, int color, int i, int j, int dx, int dy, int len) {
    int edgeSpaceNum = 0;
    int x, y;
    int k;

    //連の端が空所なら＋
    if ( i+(dx*-1) >= 0 && j+(dy*-1) >= 0 && i+(dx*-1) < size && j+(dy*-1) < size) {
      if( board[i+(dx*-1)][j+(dy*-1)] == 0 ) { edgeSpaceNum++; }
    }
    for ( k = 1; k < len; k++ ) {
      x = i+k*dx;
      y = j+k*dy;
      if ( x < 0 || y < 0 || x >= size || y >= size ) { 
        return 0; 
      } else {
        if ( board[x][y] != color ) { return 0; }
      }
    }

    x = i+k*dx;
    y = j+k*dy;

    if( x >= 0 && y >= 0 && x < size && y < size ) {
      if ( board[x][y] == 0 ) {
        edgeSpaceNum++;
      }
    }

    return edgeSpaceNum;
  }

  //----------------------------------------------------------------
  //  飛びの両端のチェック
  //----------------------------------------------------------------

  int checkJumpEdge(int[][] board, int color, int i, int j, int len) {
    int edgeSpaceNum = 0;

    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        int tmp = checkJumpEdgeDir( board, color, i, j, dx, dy, len );
        // 両端のspaceが多ければ更新
        if ( tmp > edgeSpaceNum ) {
          edgeSpaceNum = tmp;
        }
      }
    }
    return edgeSpaceNum;
  }

  int checkJumpEdgeDir(int[][] board, int color, int i, int j, int dx, int dy, int len) {
    int edgeSpaceNum = 0;

    //連の端が空所なら＋
    for ( int len1 = 2; len1 <= (len+1)/2; len1++ ) {
      int len2 = ( len + 1 ) - len1;
      // 飛びが成立するとき、両端の確認
      if ( check_run_dir(board, color, i, j, dx, dy, len1)  && check_run_dir(board, color, i, j, -dx, -dy, len2) ) {

        if ( check_out( j+dx*len1, i+dy*len1 ) || check_out( j+(-dx*len2), i+(-dy*len2) ) ) { continue; }
        if ( board[i+(dy*len1)][j+(dx*len1)] == 0 ) { edgeSpaceNum++; }
        if ( board[i+(-dy*len2)][j+(-dx*len2)] == 0 ) { edgeSpaceNum++; }
      }
    }
    return edgeSpaceNum;
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
  //----------------------------------------------------------------
  //  2連の評価
  //----------------------------------------------------------------

  int check_ren2(int[][] board, int color, int i, int j) {
    int k = 0;
    for ( int dx = -1; dx <= 1; dx++ ) {
      for ( int dy = -1; dy <= 1; dy++ ) {
        if ( dx == 0 && dy == 0 ) { continue; }
        if ( check_run_dir(board, color, i, j, dx, dy, 2) ) {
          k = checkEdgeDir( board, color, i, j, dx, dy, 2);
        }
      }
    }
    return k;
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
    if ( !check_out(i+dx, j+dy) && board[i+dx][j+dy] == color && !check_out(i+dx*2, j+dy*2) && board[i+dx*2][j+dy*2] == 0 ) {
      if ( !check_out(i-dx, j-dy) && board[i-dx][j-dy] == color*-1 ) {
        return true;
      }
    }

    //●○○にしない（ ●○ ）
    if (  !check_out(i-dx, j-dy) && board[i-dx][j-dy] == 0 && !check_out(i+dx, j+dy) && board[i+dx][j+dy] == color && !check_out(i+dx*2, j+dy*2) && board[i+dx*2][j+dy*2] == color*-1 ) {
      return true;
    }  

    return false;
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
  //  中心の点数を上げる
  //----------------------------------------------------------------

  void centerValueUp(int[][] values) {
    int num = size - 1;
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if ( values[i][j] < 0 ) { continue; }
        values[i][j] += (num/2) - Math.abs(i - (num/2));
        values[i][j] += (num/2) - Math.abs(j - (num/2));
      }
    }
  }

}
