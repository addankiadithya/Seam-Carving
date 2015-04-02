/*
 * 
 * Project 3: Seam Carving
 * 
 * Algorithms- Dr. Zhong-Hui Duan
 * 
 */

/**
 * @author Team- Adithya Addanki (aa207)
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/*
* SeamCarving accepts user Input Image and saves the processed image
* PGM images only
* Delegates the image processing to ImgLoad
*/
public class SeamCarving {
	@SuppressWarnings("unchecked")
    public static void main(String[] args)
    {
        if (args.length == 3)
        {
        	// Only process the image if there are enough arguments
            try
            {
            	long startTime=System.currentTimeMillis();
                ImgLoad il = new ImgLoad(args);
                il.loadList();
                System.out.println("Starting Process on: "+args[0]);
                //Process and remove vertical seams first
                if(il.vseams<=il.width){
	                for(int vnum=0;vnum<il.vseams;vnum++){
	                	il.findEnergy();
	                    il.calcCummEnergyV();
	                	il.removeVerticalSeam();
	                }
	            }
                else
                	// if given vseams is greater than width
                	throw new Exception("Not enough vseams to remove");
                // Process and remove horizontal seams next
                if(il.hseams<=il.height){
                	for(int hnum=0;hnum<il.hseams;hnum++){
                		il.calcCummEnergyH();
                		il.removeVerticalSeam();
                		il.ar=il.transpose(il.ar);
                	}
                }
                else
                	// if given hseams is greater than height
                	throw new Exception("Not enough hseams to remove");
                System.out.println("Done Processing on: "+args[0]);
                ImgLoad.saveImage(il);
        		long endTime=System.currentTimeMillis();
        		long millis=endTime-startTime;
        		long second = (millis / 1000) % 60;
        		long minute = (millis / (1000 * 60)) % 60;
        		long hour = (millis / (1000 * 60 * 60)) % 24;

        		String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
        		System.out.println("Time Taken: "+time);
        	}
            catch (Exception e)
            {
                System.out.println("Could not process : "+e.getMessage());
            }
        }
        else
        {
        	System.out.println("Illegal Usage");
        	System.out.println("java SeamCarving image.pgm vseams hseams");
        }
    }
}

/*
* ImgLoad accepts user Input Image and processes it
*/
class ImgLoad
{
    ArrayList<ArrayList<Integer>> ar;
    ArrayList<ArrayList<Integer>> ener;
    ArrayList<ArrayList<Double>> cummEner;
    int[][] img;
    String fname;
    String header="";
    int maxpix, width, height,vseams, hseams;

    // Setup the imagefile, vertical and horizontal seams number
    public ImgLoad(String[] ars)
    {
        fname = ars[0];
        vseams = Integer.parseInt(ars[1]);
        hseams = Integer.parseInt(ars[2]);
    }

    // Remove the vertical seam
    public void removeVerticalSeam()
    {
    	int height=cummEner.size();
    	int width=cummEner.get(0).size();
		int indmin=0;
		// find the min element in the last row
		double minele=cummEner.get(height-1).get(0);
		for (int jm = 0; jm < width; jm++) {
			if(cummEner.get(height-1).get(jm) < minele)
			{
				minele=cummEner.get(height-1).get(jm);
				indmin=jm;
			}
		}
		// remove the minimum element and propagate from bottom to top
		ArrayList<Double> tmp=cummEner.get(height-1);
		tmp.remove(indmin);
		cummEner.remove(height-1);
		cummEner.add(height-1, tmp);
		ArrayList<Integer> tmpimg=ar.get(height-1);
		tmpimg.remove(indmin);
		ar.remove(height-1);
		ar.add(height-1, tmpimg);
		for (int im = height-2; im>=0; im--) {
			int jmup=indmin; 
			int jumin=indmin-1;
			int jumax=indmin+1;
			if(jmup==0)
			{
				jumin=jmup;
			}
			else if(jmup==width-1)
			{
				jumax=jmup;
			}
			indmin=jumin;
			minele=cummEner.get(im).get(jumin);
		    while(jumin<=jumax)
		    {
		    	if(cummEner.get(im).get(jumin) < minele)
				{
					minele=cummEner.get(im).get(jumin);
					indmin=jumin;
				}
		    	jumin++;
		    }
		    tmp=cummEner.get(im);
			tmp.remove(indmin);
			cummEner.remove(im);
			cummEner.add(im, tmp);
			
			tmpimg=ar.get(im);
			tmpimg.remove(indmin);
			ar.remove(im);
			ar.add(im, tmpimg);
		}
    }

    // Calculates the cummulative energy based on energy difference of adj pixels
    public void calcCummEnergyV()
    {
    	int height=ener.size();
    	int width=ener.get(0).size();
    	cummEner= new ArrayList<ArrayList<Double>>();
    	for (int im = 0; im < height; im++) {
			ArrayList<Double> temp = new ArrayList<Double>();
			for (int jm = 0; jm < width; jm++) {
				double cenergy=0;
				if (im == 0) {
					// copy the first row
						cenergy=ener.get(im).get(jm);
				} 
				else if (jm == 0 && im != 0 && im <=height - 1) {
					/*pixels on edge of image*/
					cenergy=ener.get(im).get(jm)+
							Math.min(cummEner.get(im-1).get(jm+1),
									cummEner.get(im-1).get(jm));
					} 
				else if (jm == width - 1 && im != 0 && im <=height - 1) {
					/*pixels on edge of image*/
					cenergy=ener.get(im).get(jm)+
							Math.min(cummEner.get(im-1).get(jm-1),
									cummEner.get(im-1).get(jm));
					} 
				else {
					/*all the remaining pixels of image*/
					cenergy=ener.get(im).get(jm)+
							Math.min(cummEner.get(im-1).get(jm-1),
									Math.min(cummEner.get(im-1).get(jm),
											cummEner.get(im-1).get(jm+1)));
					}
				temp.add(cenergy);
			}
			cummEner.add(temp);
		}
    }

    // Calculates the cummulative energy by transposing
    // delegates to calcCummEnergyV()
    public void calcCummEnergyH()
    {
    	ar=transpose(ar);
    	findEnergy();
    	calcCummEnergyV();
    }

    // Transposes the arraylist
    public ArrayList<ArrayList<Integer>> transpose(ArrayList<ArrayList<Integer>> al)
    {
    	int height=al.size();
    	int width=al.get(0).size();
    	ArrayList<ArrayList<Integer>> nl=new ArrayList<ArrayList<Integer>>();
    	for (int im = 0; im < width ; im++) {
    		ArrayList<Integer> temp=new ArrayList<Integer>();
			for (int jm = 0; jm < height ; jm++) {
				temp.add(al.get(jm).get(im));
			}
			nl.add(temp);
    	}
    	return nl;
    }

    // Finds the energy map, sum of difference of energy in adj pixels
    public void findEnergy()
    {
    	int height=ar.size();
    	int width=ar.get(0).size();
    	ener= new ArrayList<ArrayList<Integer>>();
    	for (int im = 0; im < height; im++) {
			ArrayList<Integer> temp = new ArrayList<Integer>();
			for (int jm = 0; jm < width; jm++) {
				int energy=0;
				if (im == 0) {
					if (jm == 0) {
						/*corner pixels*/
						energy=Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm+1))+
								Math.abs(ar.get(im).get(jm)-
									ar.get(im+1).get(jm));
						} 
					else if (jm == width - 1) {
						energy=Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm-1))+
								Math.abs(ar.get(im).get(jm)-
									ar.get(im+1).get(jm));
						} 
					else {
						energy=Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm-1))+
								Math.abs(ar.get(im).get(jm)-
									ar.get(im+1).get(jm))+
								Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm+1));
						/*pixels on image edge*/
						}
				} 
				else if (im == height - 1) {
					if (jm == 0) {
						/*corner pixels*/
						energy=Math.abs(ar.get(im).get(jm)-
									ar.get(im-1).get(jm))+
								Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm+1));
						} 
					else if (jm == width - 1) {
						energy=Math.abs(ar.get(im).get(jm)-
									ar.get(im-1).get(jm))+
								Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm-1));
						} 
					else {
						/*pixels on edge of image*/
						energy=Math.abs(ar.get(im).get(jm)-
									ar.get(im-1).get(jm))+
								Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm-1))+
								Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm+1));
						}
				} 
				else if (jm == 0 && im != 0 && im !=height - 1) {
					/*pixels on edge of image*/
					energy=Math.abs(ar.get(im).get(jm)-
									ar.get(im-1).get(jm))+
							Math.abs(ar.get(im).get(jm)-
									ar.get(im+1).get(jm))+
							Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm+1));
					} 
				else if (jm == width - 1 && im != 0 && im != height - 1) {
					/*pixels on edge of image*/
					energy=Math.abs(ar.get(im).get(jm)-
									ar.get(im-1).get(jm))+
							Math.abs(ar.get(im).get(jm)-
									ar.get(im+1).get(jm))+
							Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm-1));
					} 
				else {
					/*all the remaining pixels of image*/
					energy=Math.abs(ar.get(im).get(jm)-
									ar.get(im-1).get(jm))+
							Math.abs(ar.get(im).get(jm)-
									ar.get(im+1).get(jm))+
							Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm-1))+
							Math.abs(ar.get(im).get(jm)-
									ar.get(im).get(jm+1));
					}
				temp.add(energy);
			}
			ener.add(temp);
		}
    }

    // Loads the image into arraylist and saves the header
    public void loadList()
    {
        int lnum,i=0,j=0;

        File infile=new File(fname);
        try
        {
        	Scanner sr = new Scanner(infile);
        	if(sr.hasNext("P2"))
        		header+=sr.nextLine()+"\n";
        	if(sr.hasNext("#.*"))
        		header+=sr.nextLine()+"\n";
        	width=sr.nextInt();
        	height=sr.nextInt();
        	img=new int[height][width];
        	maxpix=sr.nextInt();
        	ar= new ArrayList<ArrayList<Integer>>();
        	
        	for (int im = 0; im < height; im++) {
    			ArrayList<Integer> temp = new ArrayList<Integer>();
    			for (int jm = 0; jm < width; jm++) {
    				int pix=Integer.parseInt(sr.next());
    				img[im][jm]=pix;
    				temp.add(pix);
    			}
    			ar.add(temp);
    		}
        }
        catch(FileNotFoundException e)
        {
        	System.out.println("File not found");
        }
    }
    @SuppressWarnings("unchecked")
    public static void saveImage(ImgLoad il) throws IOException
    {
    	ArrayList<ArrayList<Integer>> al=(ArrayList<ArrayList<Integer>>)(il.ar.clone());

                // Save the processed image after removing horizontal and vertical seams
                FileWriter fw=new FileWriter(il.fname.substring(0,il.fname.
                					indexOf(".pgm"))+"_processed_"+il.vseams+"_"+il.
                						hseams+".pgm");
        		PrintWriter pw=new PrintWriter(fw);
        		pw.print(il.header);
        		pw.println(al.get(0).size()+"  "+al.size());
        		pw.println(il.maxpix);
        		for (ArrayList<Integer> a : al) {
        			for (Integer p : a)
        			{
        				pw.print(p + "\t");
        			}
        			pw.println();
        		}        		
        		fw.close();
    }

    // Only used for debugging, prints the arraylist in matrix format
    public static void printPixVals(ArrayList<ArrayList<Integer>> al) {
		for (ArrayList<Integer> a : al) {
			for (Integer p : a)
			{
				System.out.print(p + "\t");
			}
			System.out.println();
		}
	}
}