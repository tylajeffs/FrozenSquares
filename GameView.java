package edu.byuh.cis.cs203.numberedsquares;

import static java.lang.Math.abs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tylajeffs
 */

public class GameView extends View implements TimerListener
{

    //create fields
    private boolean calcDone;
    private float w,h;
    private int howMany;
    private Timer time;
    private Side side;

    //list of squares
    private List<NumberedSquare> squares;

    /**
     * GameView constructor. Just initializes a few variables
     * @param context - required
     */
    public GameView(Context context)
    {
        super(context);

        //set how many squares you want
        howMany = 5;

        //set the calculations done to false
        calcDone = false;
        squares = new ArrayList<>();

        //instantiate timer
        time = new Timer();

        //add GameView class to the observers using this
        time.addObserver(this);

    }



    /**
     * All the rendering happens here. The first time onDraw is called, we also do some
     * one-time initialization of the graphics objects (since the width and height of
     * the screen are not known until onDraw is called).
     * @param c
     */
    @Override
    public void onDraw(Canvas c)
    {

        if (!calcDone)
        {
            //get the width and height
            w = c.getWidth();
            h = c.getHeight();

            //create the squares
            createSquares(howMany);

            //set calcDone to true
            calcDone = true;
        }

        //set the background color
        c.drawColor(Color.rgb(203, 247, 207));

        //draw each of the squares in the list
        for (NumberedSquare ns : squares)
        {
            //draw the square
            ns.draw(c);
        }

    }





    /**
     * Helper method for creating five NumberedSquare objects
     * @param num the number of squares you want created
     */
    private void createSquares(int num)
    {
        //clear the list of squares
        squares.clear();

        //reset the counter back to 1
        NumberedSquare.resetCounter();

        //create 5 new numbered squares
        for (int i=0; i<num; i++)
        {

            //create new numbered square
            NumberedSquare ns = new NumberedSquare(w, h);

            //create a boolean to see if they overlap or not - assume they don't overlap
            boolean overlaps = false;

            for(NumberedSquare square:squares)
            {

                if(ns.overlaps(square))
                {
                    //set overlaps to true
                    overlaps = true;

                    //decrease the counter to show that it got deleted - need to keep numbers correct
                    ns.decreaseCounter();

                    //subtract 1 from i so the code will make a new square
                    //to replace the one that overlapped
                    i--;

                    //break the loop and try again
                    break;
                }

            }

            //if it doesn't overlap, add it to the squares list
            if(overlaps == false)
            {
                //add ns to the squares list
                squares.add(ns);

                //add squares to observers
                for(NumberedSquare square : squares)
                {
                    time.addObserver(square);
                }
            }

        }

    }






    /**
     * this method gets called automatically whenever a user taps the screen
     *
     * @param m - mandatory parameter, motionevent
     * @return always returns true
     */

    @Override
    public boolean onTouchEvent(MotionEvent m)
    {
        //variables
        boolean squareTapped = false;
        int squareId = 0;




        //figure out if screen was tapped (based on release)
        if(m.getAction() == MotionEvent.ACTION_UP)
        {
            //send message to Logcat saying the screen was tapped
            Log.d("log", "you tapped the screen");

            //find the coordinates that were tapped
            float x = m.getX();
            float y = m.getY();



            for(NumberedSquare square:squares)
            {
                //if the square contains the coordinates, freeze
                if(square.contains(x,y))
                {
                    //set squareTapped to true
                    squareTapped = true;

                    //store the id of the square that was tapped
                    squareId = square.id;

                    //send message to Logcat saying the square was tapped
                    Log.d("log", "you tapped square " + squareId);


                    //freeze the square
                    square.freeze();

                    break;
                }
            }


            //if a square wasn't tapped, redraw all the squares
            if(!squareTapped)
            {
                //create the squares again in new locations
                createSquares(howMany);
            }



            //redraw the squares in the list using invalidate method
            invalidate();
        }

        //always returns true
        return true;
    }




    /**
     * Override doSomething from the TimerListener interface to redraw the screen
     */
    @Override
    public void doSomething()
    {
        //check to see if any squares are bumping into each other
        for(NumberedSquare a: squares)
        {
            for(NumberedSquare b: squares)
            {
                //code to make sure there is no redundancy in checking squares against each other
                if(a.id > b.id)
                {
                    //check to see if the two squares overlap
                    if(a.overlaps(b))
                    {
                        //if the sides touching are top or bottom, exchange y velocity
                        if(findSideHit(a,b) == Side.TOP || findSideHit(b,a) == Side.TOP || findSideHit(a,b) == Side.BOTTOM || findSideHit(b,a) == Side.BOTTOM)
                        {
                            //check if either of the squares are frozen
                            if(a.frozen)
                            {
                                //reverse b's y velocity
                                b.velocity.y *= -1;
                            }
                            else if(b.frozen)
                            {
                                //reverse a's y velocity
                                a.velocity.y *= -1;
                            }
                            else
                            {
                                //force them apart code keeps breaking!
                                //force them apart
                                //forceApart(a,b);

                                //exchange y velocities
                                exchangeYVelocity(a,b);
                            }
                        }

                        //if sides touching are left or right, exchange x velocity
                        if(findSideHit(a,b) == Side.LEFT || findSideHit(b,a) == Side.LEFT|| findSideHit(a,b) == Side.RIGHT || findSideHit(b,a) == Side.RIGHT)
                        {
                            //check if either of the squares are frozen
                            if(a.frozen)
                            {
                                //reverse b's x velocity
                                b.velocity.x *= -1;
                            }
                            else if(b.frozen)
                            {
                                //reverse a's x velocity
                                a.velocity.x *= -1;
                            }
                            else
                            {
                                // force them apart code keeps breaking!
                                //force them apart
                                //forceApart(a,b);

                                //exchange x velocities
                                exchangeXVelocity(a,b);
                            }

                        }

                    }

                }

            }

        }



        //make the screen redraw by calling invalidate
        invalidate();
    }




    /**
     * exchanges the Y velocities of the numbered squares passed in
     * @param a numbered square
     * @param b numbered square
     */
    public void exchangeYVelocity(NumberedSquare a, NumberedSquare b)
    {
        //store square a's y velocity
        float aVel = a.velocity.y;

        //exchange velocities
        a.velocity.y = b.velocity.y;
        b.velocity.y = aVel;

    }




    /**
     * exchanges the X velocities of the numbered squares passed in
     * @param a numbered square
     * @param b numbered square
     */
    public void exchangeXVelocity(NumberedSquare a, NumberedSquare b)
    {
        //store square a's x velocity
        float aVel = a.velocity.x;

        //exchange velocities
        a.velocity.x = b.velocity.x;
        b.velocity.x = aVel;

    }





    /**
     * method to find the side that got hit from two squares colliding
     *
     * @param a numbered square to be tested
     * @param b numbered square to be tested
     * @return an emum, which side got hit on square a
     */
    public Side findSideHit(NumberedSquare a, NumberedSquare b)
    {
        //initialize hitSide to empty (for debugging)
        Side hitSide = Side.EMPTY;

        //find the difference between sides to see which side is hitting (the smallest ones are hitting)
        float rightDif = abs(a.squareBounds.right - b.squareBounds.left);
        float leftDif = abs(a.squareBounds.left - b.squareBounds.right);
        float topDif = abs(a.squareBounds.top - b.squareBounds.bottom);
        float bottomDif = abs(a.squareBounds.bottom - b.squareBounds.top);

        //find the smallest one
        float smallest = Math.min(Math.min(rightDif,leftDif),Math.min(topDif,bottomDif));



        //assign enum based on which is smallest

        //right
        if(smallest == rightDif)
        {
            //assign right
            hitSide = Side.RIGHT;

        }
        //left
        if(smallest == leftDif)
        {
            //assign left
            hitSide = Side.LEFT;

        }
        //top
        if(smallest == topDif)
        {
            //assign top
            hitSide = Side.TOP;
        }
        //bottom
        if(smallest == bottomDif)
        {
            //assign bottom
            hitSide = Side.BOTTOM;
        }

        //return the side
        return hitSide;
    }




public void forceApart(NumberedSquare a, NumberedSquare b)
{
    switch (side)
    {
        case LEFT:
            a.setLeft(b.squareBounds.right + 1);
            b.setRight(b.squareBounds.left - 1);
            break;
        case RIGHT:
            a.setRight(b.squareBounds.left - 1);
            b.setLeft(a.squareBounds.right + 1);
            break;
        case TOP:
            this.setTop((int)b.squareBounds.bottom + 1);
            b.setBottom(a.squareBounds.top - 1);
            break;
        case BOTTOM:
            this.setBottom((int)b.squareBounds.top - 1);
            b.setTop(a.squareBounds.bottom + 1);
    }
}






}


