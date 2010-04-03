/**
 *
 * This class is based on streamgraph_generator.pde which was exported
 * as an application, disassembled, and modified.
 * 
 **/

import processing.core.PApplet;

public class StreamgraphGenerator extends PApplet
{
  boolean isGraphCurved = true;
  int seed = 28;

  float DPI = 300.0F;
  float widthInches = 3.5F;
  float heightInches = 0.7F;
  int numLayers = 50;
  int layerSize = 100;
  DataSource data;
  LayerLayout layout;
  LayerSort ordering;
  ColorPicker coloring;
  Layer[] layers;

  public void setup()
  {
    size(PApplet.parseInt(this.widthInches * this.DPI), PApplet.parseInt(this.heightInches * this.DPI));
    smooth();
    noLoop();

    // GENERATE DATA
    data     = new LateOnsetDataSource();
    //data     = new BelievableDataSource();

    // ORDER DATA
    ordering = new LateOnsetSort();
    //ordering = new VolatilitySort();
    //ordering = new InverseVolatilitySort();
    //ordering = new BasicLateOnsetSort();
    //ordering = new NoLayerSort();

    // LAYOUT DATA
    layout   = new StreamLayout();
    //layout   = new MinimizedWiggleLayout();
    //layout   = new ThemeRiverLayout();
    //layout   = new StackLayout();

    // COLOR DATA
    coloring = new LastFMColorPicker(this, "layers-nyt.jpg");
    //coloring = new LastFMColorPicker(this, "layers.jpg");
    //coloring = new RandomColorPicker(this);

    //=========================================================================

    // calculate time to generate graph
    long l1 = System.currentTimeMillis();

    // generate graph
    this.layers = this.data.make(this.numLayers, this.layerSize);
    this.layers = this.ordering.sort(this.layers);
    this.layout.layout(this.layers);
    this.coloring.colorize(this.layers);

    // fit graph to viewport
    scaleLayers(this.layers, 1, this.height - 1);

    // give report
    long l2 = System.currentTimeMillis() - l1;
    int i = this.layers.length;
    int j = this.layers[0].size.length;
    println("Data has " + i + " layers, each with " + 
      j + " datapoints.");
    println("Layout Method: " + this.layout.getName());
    println("Ordering Method: " + this.ordering.getName());
    println("Coloring Method: " + this.layout.getName());
    println("Elapsed Time: " + l2 + "ms");
  }

  // adding a pixel to the top compensate for antialiasing letting
  // background through. This is overlapped by following layers, so no
  // distortion is made to data.
  // detail: a pixel is not added to the top-most layer
  // detail: a shape is only drawn between it's non 0 values
  public void draw()
  {
    int i = this.layers.length;
    int j = this.layers[0].size.length;

    int i1 = i - 1;

    background(255);
    noStroke();

    // calculate time to draw graph
    long l1 = System.currentTimeMillis();

    // generate graph
    for (int i2 = 0; i2 < i; ++i2) {
      int k = max(0, this.layers[i2].onset - 1);
      int l = min(j - 1, this.layers[i2].end);
      //      ((i2 == i1) ? 0 : 1);

      // set fill color of layer
      fill(this.layers[i2].rgb);

      // draw shape
      beginShape();

      // draw top edge, left to right
      graphVertex(k, this.layers[i2].yTop, this.isGraphCurved, i2 == i1);
      for (int i3 = k; i3 <= l; ++i3) {
        graphVertex(i3, this.layers[i2].yTop, this.isGraphCurved, i2 == i1);
      }
      graphVertex(l, this.layers[i2].yTop, this.isGraphCurved, i2 == i1);

      // draw bottom edge, right to left
      graphVertex(l, this.layers[i2].yBottom, this.isGraphCurved, false);
      for (int i3 = l; i3 >= k; --i3) {
        graphVertex(i3, this.layers[i2].yBottom, this.isGraphCurved, false);
      }
      graphVertex(k, this.layers[i2].yBottom, this.isGraphCurved, false);

      endShape(2);
    }

    // give report
    long l2 = System.currentTimeMillis() - l1;
    println("Draw Time: " + l2 + "ms");
  }

  public void graphVertex(int paramInt, float[] paramArrayOfFloat, boolean paramBoolean1, boolean paramBoolean2) {
    float f1 = map(paramInt, 0.0F, this.layerSize - 1, 0.0F, this.width);
    float f2 = paramArrayOfFloat[paramInt] - ((paramBoolean2) ? 1 : 0);
    if (paramBoolean1)
      curveVertex(f1, f2);
    else
      vertex(f1, f2);
  }

  public void scaleLayers(Layer[] paramArrayOfLayer, int paramInt1, int paramInt2)
  {
    // Figure out max and min values of layers.
    float f1 = Float.MAX_VALUE;
    float f2 = Float.MIN_VALUE;
    for (int i = 0; i < paramArrayOfLayer[0].size.length; ++i) {
      for (int j = 0; j < paramArrayOfLayer.length; ++j) {
        f1 = min(f1, paramArrayOfLayer[j].yTop[i]);
        f2 = max(f2, paramArrayOfLayer[j].yBottom[i]);
      }
    }

    float f3 = (paramInt2 - paramInt1) / (f2 - f1);
    for (int j = 0; j < paramArrayOfLayer[0].size.length; ++j)
      for (int k = 0; k < paramArrayOfLayer.length; ++k) {
        paramArrayOfLayer[k].yTop[j] = (paramInt1 + f3 * (paramArrayOfLayer[k].yTop[j] - f1));
        paramArrayOfLayer[k].yBottom[j] = (paramInt1 + f3 * (paramArrayOfLayer[k].yBottom[j] - f1));
      }
  }

  public void keyPressed()
  {
    if (this.keyCode == 10) {
      println();
      println("Rendering image...");
      String str = "images/streamgraph-" + dateString() + ".png";
      save(str);
      println("Rendered image to: " + str);
    }

    // hack for un-responsive non looping p5 sketches
    if (this.keyCode == 27)
      redraw();
  }

  public String dateString()
  {
    return year() + "-" + nf(month(), 2) + "-" + nf(day(), 2) + "@" + 
      nf(hour(), 2) + "-" + nf(minute(), 2) + "-" + nf(second(), 2);
  }

  public static void main(String[] paramArrayOfString) {
    PApplet.main(new String[] { "--bgcolor=#DFDFDF", "StreamgraphGenerator" });
  }
}
