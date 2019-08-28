
package Samples;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;

public class findRoute
{

    	//===================================================================================
	// read txt files from local folder and load into 2darray
    	public char[][] readFile(String path)
    	{

		int i = 0;
		int j = 0;
		int w = 0;
		int h = 0;
		int startx = 0;
		int starty = 0;
		int endx = 0;
		int endy = 0;

		try
		{
			String xx = "";
			RandomAccessFile cp = new RandomAccessFile(path, "r");

			// get height and width
			xx=cp.readLine();		
			String[] wh = xx.split(" ");
			w = Integer.parseInt(wh[0]);
			h = Integer.parseInt(wh[1]);

			// get start x and y poses
			xx=cp.readLine();		
			wh = xx.split(" ");
			startx = Integer.parseInt(wh[0]);
			starty = Integer.parseInt(wh[1]);

			// get end x and y poses
			xx=cp.readLine();		
			wh = xx.split(" ");
			endx = Integer.parseInt(wh[0]);
			endy = Integer.parseInt(wh[1]);

			//now we know width and height create a 2darray
			char[][] twodArray = new char[h][w];
			String[] oline = new String[w];

			//System.out.println("reading file w="+w+" h="+h+" xx="+xx+" ol="+oline.length);

			i=0;
			while ((xx=cp.readLine())!=null){
				xx = xx.replace("1","#");
				oline = xx.split(" ");
				j=0;
				while (j < w){
					if (oline[j].equals("0")) oline[j] = " ";
					twodArray[i][j] = oline[j].charAt(0);
					if (j==startx && i==starty) twodArray[i][j] = 'S';
					if (j==endx && i==endy) twodArray[i][j] = 'E';
					j++;
				}						
				i++;
			}
			cp.close();

			return twodArray;
		}
		catch (IOException ioe)
		{
        	    	System.out.println("reading file IOException - "+ioe.getMessage());
	    		return null;
		}
		catch (Exception e)
		{
            		System.out.println("reading files Exception - i="+i+" j="+j+" "+e.getMessage());
	    		return null;
		}
    	}

	//================================================================================
	// this method displays the 2darray from the char passed with the walls
	public void displayGrid(getRoute gr)
	{
		String line = "";
		int i = 0;
		int j = 0;
		int ylen = gr.getYlen();
		int xlen = gr.getXlen();

		//System.out.println("xlen="+xx.length+" ylen="+xx[0].length);

		while (i < xlen){
			j = 0;
			line = "";
			while (j < ylen){
				line += gr.get2dArray(i,j);
				j++;
			}
			System.out.println(line);
			i++;
		}

	}

	//=========================================================================
	// this section finds the start pos, once found then passes the pos to the
	// getroute method, then exits
	public void startRoute(char[][] xx)
	{
		int i = 0;
		int j = 0;
		int ylen = xx[0].length;
		int xlen = xx.length;
		int startx = 0;
		int starty = 0;

		while (i < xlen){
			j = 0;
			while (j < ylen){
				if (xx[i][j] == 'S'){
					startx = i;
					starty = j;
					i = xlen;
					callGetRoute(xx,startx,starty);
					break;
				}
				j++;
			}
			i++;
		}
	}

	//=======================================================================================
	// this method calls solveroute which loops around to find the path, if true is returned then 
	// the end has been found, if false is returned then a deadend has been found so call the reverse 
	// method and loops until end is found or xlen*ylen iterations have been done. The start pos
	// are passed
	public void callGetRoute(char[][] xx, int startx, int starty)
	{

		int i = 0;
		int j = 0;
		char ver = ' ';

		// instantiate sub class, creates the2dArray
		getRoute gr = new getRoute(xx);
		findRoute fr = new findRoute();

		//initally set to S
		gr.setXlen(xx.length);
		gr.setYlen(xx[0].length);
		gr.setNewStartX(startx);
		gr.setNewStartY(starty);

		//System.out.println("xlen="+gr.getXlen()+" ylen="+gr.getYlen());
		//fr.displayGrid(gr);

		i = 0;
		while (i < (gr.getXlen()*gr.getYlen()) && !gr.getRouteFound()){
			//if deadend newstartx&y are set to last #
			gr = fr.callFollowRoute(gr,fr);
			if (!gr.getRouteFound()) {
				// newstartx&y are set to branch which is used by followRoute
				gr = fr.callReverseAlongX(gr,fr);
			}
			i++;
		}

		if (gr.getRouteFound()){
			//if true then clear and display
			gr = fr.removeAllElse(gr);

			fr.displayGrid(gr);
			System.out.println("*** Finished ***");
		}else{
			System.out.println("*******  A solution could not be found  *************");
			fr.displayGrid(gr);
		}
		System.out.println(" ");
	}

	//=================================================================================
	// from followroute a deadend has been found so this will loop back until space found
	public getRoute callReverseAlongX(getRoute gr, findRoute fr) {

		//System.out.println("rax - start row="+gr.getNewStartX()+" col="+gr.getNewStartY());
		gr.setRow(gr.getNewStartX());
		gr.setCol(gr.getNewStartY());
		gr.setExitLoop(false);
		int i = 0;
		while (i < (gr.getXlen()*gr.getYlen()) && !gr.getExitLoop()){
			gr = fr.reverseAlongX(gr,'!');
			i++;
		}
		return gr;
	}

	//=================================================================================
	// this will loop around until deadend or E is found
	public getRoute callFollowRoute(getRoute gr, findRoute fr) {

		//System.out.println("cfr - start");
		gr.setRow(gr.getNewStartX());
		gr.setCol(gr.getNewStartY());
		gr.setExitLoop(false);
		int i = 0;
		while (i < (gr.getXlen()*gr.getYlen()) && !gr.getExitLoop()){
			gr = fr.followRoute(gr, 'X');
			i++;
		}
		return gr;
	}

	//=================================================================================
	// this section is looped until spaceis found and is designed to remove any dead ends from the array
	// as it reverses the X's are set to ! so the same route cannot be used again.
	// when finished false is always returned
	public getRoute reverseAlongX(getRoute gr, char ver) {

		int row = gr.getRow();
		int col = gr.getCol();

		// for wrap around check col and row to see if around the edge.
		char right = ' ';
		char left = ' ';
		char up =  ' ';
		char down = ' ';
		if (col == gr.getMaxCol() - 1) right = gr.get2dArray(row,0); else right = gr.get2dArray(row,col+1);
		if (col == 0) left = gr.get2dArray(row,gr.getMaxCol() - 1); else left = gr.get2dArray(row,col-1);
		if (row == 0) up = gr.get2dArray(gr.getMaxRow() - 1,col); else up = gr.get2dArray(row-1,col);
		if (row == gr.getMaxRow() - 1) down = gr.get2dArray(0,col); else down = gr.get2dArray(row+1,col);

		char cur = gr.get2dArray(row,col);

		// if a place to branch has been found set the current pos to X
		// and set new start pos's
  		if (right == ' ' || left == ' ' || up == ' ' || down == ' ') {
			//System.out.println("rev branch found ("+row + ":" + col+") ele="+this.the2dArray[row][col]);
  			if (cur == '!') {
				gr.set2dArray('X',row,col);
  			}
			gr.setNewStartX(row);
			gr.setNewStartY(col);
			gr.setExitLoop(true);
			gr.setRouteFound(false);
    			return gr;
  		}
  		//System.out.println("rev - "+"("+row + ":" + col+") ver="+ver+" rlud="+
		//	right+left+up+down);
  		if (cur == 'X') {
			gr.set2dArray(ver,row,col);
  		}
  		if (right == 'X') {
			gr.setRow(row);
			gr.setCol(col+1);
			if (gr.getCol() == gr.getMaxCol()) gr.setCol(0);
			gr.set2dArray(ver,gr.getRow(),gr.getCol());
			gr.setRouteFound(false);
    			return gr;
  		}
  		if (down == 'X') {
			gr.setRow(row+1);
			gr.setCol(col);
			gr.setRouteFound(false);
			if (gr.getRow() == gr.getMaxRow()) gr.setRow(0);
			gr.set2dArray(ver,gr.getRow(),gr.getCol());
    			return gr;
  		}
  		if (left == 'X') {
			gr.setRow(row);
			gr.setCol(col-1);
			if (gr.getCol() == 0) gr.setCol(gr.getMaxCol() - 1);
			gr.set2dArray(ver,gr.getRow(),gr.getCol());
			gr.setRouteFound(false);
   			return gr;
  		}
  		if (up == 'X') {
			gr.setRow(row-1);
			gr.setCol(col);
			if (gr.getRow() == 0) gr.setRow(gr.getMaxRow() - 1);
			gr.set2dArray(ver,gr.getRow(),gr.getCol());
			gr.setRouteFound(false);
    			return gr;
  		}
  		if (right == 'S') {
			gr.setRow(row);
			gr.setCol(col+1);
			gr.setRouteFound(false);
    			return gr;
  		}
  		if (down == 'S') {
			gr.setRow(row+1);
			gr.setCol(col);
			gr.setRouteFound(false);
    			return gr;
  		}
  		if (left == 'S') {
			gr.setRow(row);
			gr.setCol(col-1);
			gr.setRouteFound(false);
   			return gr;
  		}
  		if (up == 'S') {
			gr.setRow(row-1);
			gr.setCol(col);
			gr.setRouteFound(false);
    			return gr;
  		}

		gr.setNewStartX(row);
		gr.setNewStartY(col);
		//System.out.println("rev - NO branch found ("+row + ":" + col+") ele="+
		//	gr.get2dArray(row,col)+" rlud="+right+left+up+down);
 
		gr.setRouteFound(false);
		gr.setExitLoop(true);
  		return gr;
	}
	//=====================================================================================
	// This is a method is loops until deadend or E which ideally should finish when E is found 
	// and true is then returned, but if a dead end is found then false is returned along with 
	// new start pos's the elements of the 2darray are set to X, added wrap around processing
	public getRoute followRoute(getRoute gr, char ver) 
	{
		int row = gr.getRow();
		int col = gr.getCol();

		//for wrap around check col and row to see if on the edge and position at opposite end		
		char right = ' ';
		char left = ' ';
		char up = ' ';
		char down = ' ';
		char cur = ' ';
		if (col == 0 || col == gr.getMaxCol() - 1){
			if (col == 0) col = gr.getMaxCol() - 1; else col = 0;
			cur = gr.get2dArray(row,col);
			if (cur != ' '){
				if (col == 0) col = gr.getMaxCol() - 1; else col = 0;		//reverse back
			}
			if (col == 0){ 
				left = '#'; 
				right = gr.get2dArray(row,col+1);
			}else{ 
				right = '#';
				left = gr.get2dArray(row,col-1);
			}
			up = gr.get2dArray(row-1,col);
			down = gr.get2dArray(row+1,col);
			gr.set2dArray(ver,row,col);

		}else if (row == 0 || row == gr.getMaxRow() - 1){
			if (row == 0) row = gr.getMaxRow() - 1; else row = 0;
			cur = gr.get2dArray(row,col);
			if (cur != ' '){
				if (row == 0) row = gr.getMaxRow() - 1; else row = 0;
			}
			if (row == 0){ 
				up = '#'; 
				down = gr.get2dArray(row+1,col);
			}else{ 
				down = '#';
				up = gr.get2dArray(row-1,col);
			}
			left = gr.get2dArray(row,col-1);
			right = gr.get2dArray(row,col+1);
			gr.set2dArray(ver,row,col);
		}else{
			right = gr.get2dArray(row,col+1);
			left = gr.get2dArray(row,col-1);
			up = gr.get2dArray(row-1,col);
			down = gr.get2dArray(row+1,col);
		}

  		//System.out.println("position=>"+"("+row + ":" + col+") ver="+ver+" rlud="+right+left+up+down);
  		if (right == 'E' || left == 'E' || up == 'E' || down == 'E') {
			System.out.println("end found ("+row + ":" + col+") ver="+ver);
			gr.setRouteFound(true);
			gr.setExitLoop(true);
    			return gr;
  		}

  		if (right == ' ') {
			gr.setRow(row);
			gr.setCol(col+1);
			gr.set2dArray(ver,gr.getRow(),gr.getCol());
			gr.setRouteFound(false);
    			return gr;
  		}
  		if (down == ' ') {
			gr.setRow(row+1);
			gr.setCol(col);
			gr.set2dArray(ver,gr.getRow(),gr.getCol());
			gr.setRouteFound(false);
    			return gr;
  		}
  		if (left == ' ') {
			gr.setRow(row);
			gr.setCol(col-1);
			gr.set2dArray(ver,gr.getRow(),gr.getCol());
			gr.setRouteFound(false);
   			return gr;
  		}
  		if (up == ' ') {
			gr.setRow(row-1);
			gr.setCol(col);
			gr.set2dArray(ver,gr.getRow(),gr.getCol());
			gr.setRouteFound(false);
    			return gr;
  		}
		gr.setNewStartX(row);
		gr.setNewStartY(col);

		//System.out.println("solve deadend found ("+row + ":" + col+") ele="+
		//	gr.get2dArray(row,col));

		gr.setRouteFound(false);
		gr.setExitLoop(true);
  		return gr;
	}
	//======================================================================================
	// this method is non-recursive and just traverses the 2darray from top left to bottom right
	// and removes all non valid chars, mainly '!'
	public getRoute removeAllElse(getRoute gr)
	{

		int i = 0;
		int j = 0;
		int xlen = gr.getXlen();
		int ylen = gr.getYlen();

		while (i < (xlen)){
			j = 0;
			while (j < ylen){
				if (gr.get2dArray(i,j) !='#' && gr.get2dArray(i,j) !='S' && 
					gr.get2dArray(i,j)!='E' &&
					gr.get2dArray(i,j)!='X') gr.set2dArray(' ',i,j); 
				j++;
			}
			i++;
		}
		return gr;
	}
    	//===================================================================================
	// main section calling read file method which returns 2darray, if problem reading null
	// is returned, if ok the startroute is called which processes the array
    	static public void main(String[] args)
    	{
		String path = args[0];
		findRoute fr = new findRoute();
		char[][] xx = fr.readFile(path);
		if (xx != null){
			System.out.println("Processing - "+path);
			fr.startRoute(xx);
		}
 	}

	//============================================================================
	// sub class is used so when instantiated the 2darray can be created with variable size
	// and is encapsulated, the contructor passes the 2darray
	public class getRoute
	{
		private int maxRow = 0;
		private int maxCol = 0;
		private int row = 0;
		private int col = 0;
		private char[][] the2dArray = new char[row][col];
		private int newstartx = 0;
		private int newstarty = 0;
		private int xlen = 0;
		private int ylen = 0;
		private boolean routeFound = false;
		private boolean exitLoop = false;

		public getRoute(char[][] xx){
			this.row = xx.length;
			this.col = xx[0].length;
			maxRow = xx.length;
			maxCol = xx[0].length;
			this.the2dArray = xx;
		}

		public char get2dArray(int i, int j){
			return this.the2dArray[i][j];
		}
		public void set2dArray(char xx, int i, int j){
			this.the2dArray[i][j] = xx;
		}

		public int getNewStartX(){
			return this.newstartx;
		}
		public void setNewStartX(int x){
			this.newstartx = x;
		}

		public int getNewStartY(){
			return this.newstarty;
		}
		public void setNewStartY(int x){
			this.newstarty = x;
		}

		public int getXlen(){
			return this.xlen;
		}
		public void setXlen(int x){
			this.xlen = x;
		}
		public int getYlen(){
			return this.ylen;
		}
		public void setYlen(int x){
			this.ylen = x;
		}

		public boolean getRouteFound(){
			return this.routeFound;
		}
		public void setRouteFound(boolean x){
			this.routeFound = x;
		}

		public boolean getExitLoop(){
			return this.exitLoop;
		}
		public void setExitLoop(boolean x){
			this.exitLoop = x;
		}

		public int getRow(){
			return this.row;
		}
		public void setRow(int x){
			this.row = x;
		}
		public int getCol(){
			return this.col;
		}
		public void setCol(int x){
			this.col = x;
		}
		public int getMaxRow(){
			return this.maxRow;
		}
		public int getMaxCol(){
			return this.maxCol;
		}

	}
}
