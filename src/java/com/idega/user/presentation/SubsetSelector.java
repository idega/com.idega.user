package com.idega.user.presentation;

import com.idega.util.IWColor;
import com.idega.idegaweb.browser.presentation.IWBrowserView;
import com.idega.presentation.*;
import com.idega.event.IWPresentationEvent;
import com.idega.user.event.PartitionSelectEvent;
import com.idega.presentation.text.Link;

/**
 * <p>Title: idegaWeb</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: idega Software</p>
 * @author <a href="gummi@idega.is">Gu�mundur �g�st S�mundsson</a>
 * @version 1.0
 */

public class SubsetSelector extends PresentationObjectContainer implements IWBrowserView
{
	private static final String spacer = " ";
	private int _maxShowedPartitions = 6;
	private int _maxPartitions;
	private int _firstPartition = 0;

	private int _selectedSubset = 0;
	private int _subsetSize;
	private int _size = 0;

	private String _controlTarget = null;
	private IWPresentationEvent _contolEvent = null;


    public SubsetSelector(int subsetSize, int size, int maxShowedPartitions)
    {
		this._maxShowedPartitions = maxShowedPartitions;
		this._size = size;
		this._maxPartitions= size/subsetSize + ((size%subsetSize > 0)?1:0);
		this._subsetSize = subsetSize;
    }

	public void setFirstSubset(int index){
	    this._firstPartition = index;
	}

	public void setSelectedSubset(int index){
	    this._selectedSubset = index;
	}

	public void setControlEventModel(IWPresentationEvent model){
	    this._contolEvent = model;
	}

	public void setControlTarget(String controlTarget){
	    this._controlTarget = controlTarget;
	}


	public void main(IWContext iwc) throws Exception
	{
		Layer partitionSelection = new Layer();
		partitionSelection.setHorizontalAlignment("center");
                partitionSelection.setBackgroundColor(new IWColor(230,230,230).getHexColorString());


		if (this._size > this._subsetSize){
			for( int i = this._firstPartition; ((i < this._maxPartitions)&&((i-this._firstPartition) < this._maxShowedPartitions)); i++)
			{
				if(this._firstPartition == i && this._firstPartition != 0)
				{
					Link begin = new Link();

					begin.setText("<");
					PartitionSelectEvent event = new PartitionSelectEvent();
					//event.setSource(this.getLocation());
          event.setSource(this);
					int newFirstPartition = Math.max(0,this._firstPartition-this._maxShowedPartitions);
					event.setFirstPartitionIndex(newFirstPartition);
					event.setPartitionSize(this._subsetSize);
					int newSelectedPartition = newFirstPartition+this._maxShowedPartitions-1;
					event.setSelectedPartition(newSelectedPartition);
					begin.addEventModel(event);
					if (this._controlTarget != null)
					{
						begin.setTarget(this._controlTarget);
					}
					if (this._contolEvent != null)
					{
						begin.addEventModel(this._contolEvent);
					}


					begin.addEventModel(this._contolEvent);

					partitionSelection.add(begin);

					partitionSelection.add(spacer);
				}

				Link l = new Link();
				if(i != this._firstPartition){
					partitionSelection.add(spacer);
				}
				l.setText(((i*this._subsetSize)+1)+"-"+(((i+1)*this._subsetSize)));
				PartitionSelectEvent event = new PartitionSelectEvent();
				event.setSource(this.getLocation());
				event.setPartitionSize(this._subsetSize);
				event.setFirstPartitionIndex(this._firstPartition);
				event.setSelectedPartition(i);
				l.addEventModel(event);
				if (this._controlTarget != null)
				{
					l.setTarget(this._controlTarget);
				}
				if (this._contolEvent != null)
				{
					l.addEventModel(this._contolEvent);
				}
				if(i == this._selectedSubset)
				{
					l.setBold();
				}
				partitionSelection.add(l);


				if(((i == this._maxPartitions-1)||((i-this._firstPartition) == this._maxShowedPartitions-1)) && this._maxPartitions > (i+1))
				{
					partitionSelection.add(spacer);
					Link end = new Link();
					end.setText(">");
					PartitionSelectEvent event2 = new PartitionSelectEvent();
					event2.setSource(this.getLocation());
					int newFirstPartition = Math.min(this._maxPartitions-this._maxShowedPartitions,this._firstPartition+this._maxShowedPartitions);
					event2.setFirstPartitionIndex(newFirstPartition);
					event2.setPartitionSize(this._subsetSize);
					event2.setSelectedPartition(newFirstPartition);
					end.addEventModel(event2);
					if (this._controlTarget != null)
					{
						end.setTarget(this._controlTarget);
					}
					if (this._contolEvent != null)
					{
						end.addEventModel(this._contolEvent);
					}
					partitionSelection.add(end);
				}
			}
			this.add(partitionSelection);
		}
	}
}