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
 * @author <a href="gummi@idega.is">Guðmundur Ágúst Sæmundsson</a>
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
		_maxShowedPartitions = maxShowedPartitions;
		_size = size;
		_maxPartitions= size/subsetSize + ((size%subsetSize > 0)?1:0);
		_subsetSize = subsetSize;
    }

	public void setFirstSubset(int index){
	    _firstPartition = index;
	}

	public void setSelectedSubset(int index){
	    _selectedSubset = index;
	}

	public void setControlEventModel(IWPresentationEvent model){
	    _contolEvent = model;
	}

	public void setControlTarget(String controlTarget){
	    _controlTarget = controlTarget;
	}


	public void main(IWContext iwc) throws Exception
	{
		Layer partitionSelection = new Layer();
		partitionSelection.setHorizontalAlignment("center");
                partitionSelection.setBackgroundColor(new IWColor(230,230,230).getHexColorString());


		if (_size > _subsetSize){
			for( int i = _firstPartition; ((i < _maxPartitions)&&((i-_firstPartition) < _maxShowedPartitions)); i++)
			{
				if(_firstPartition == i && _firstPartition != 0)
				{
					Link begin = new Link();

					begin.setText("<");
					PartitionSelectEvent event = new PartitionSelectEvent();
					//event.setSource(this.getLocation());
          event.setSource(this);
					int newFirstPartition = Math.max(0,_firstPartition-_maxShowedPartitions);
					event.setFirstPartitionIndex(newFirstPartition);
					event.setPartitionSize(_subsetSize);
					int newSelectedPartition = newFirstPartition+_maxShowedPartitions-1;
					event.setSelectedPartition(newSelectedPartition);
					begin.addEventModel(event);
					if (_controlTarget != null)
					{
						begin.setTarget(_controlTarget);
					}
					if (_contolEvent != null)
					{
						begin.addEventModel(_contolEvent);
					}


					begin.addEventModel(_contolEvent);

					partitionSelection.add(begin);

					partitionSelection.add(spacer);
				}

				Link l = new Link();
				if(i != _firstPartition){
					partitionSelection.add(spacer);
				}
				l.setText(((i*_subsetSize)+1)+"-"+(((i+1)*_subsetSize)));
				PartitionSelectEvent event = new PartitionSelectEvent();
				event.setSource(this.getLocation());
				event.setPartitionSize(_subsetSize);
				event.setFirstPartitionIndex(_firstPartition);
				event.setSelectedPartition(i);
				l.addEventModel(event);
				if (_controlTarget != null)
				{
					l.setTarget(_controlTarget);
				}
				if (_contolEvent != null)
				{
					l.addEventModel(_contolEvent);
				}
				if(i == _selectedSubset)
				{
					l.setBold();
				}
				partitionSelection.add(l);


				if(((i == _maxPartitions-1)||((i-_firstPartition) == _maxShowedPartitions-1)) && _maxPartitions > (i+1))
				{
					partitionSelection.add(spacer);
					Link end = new Link();
					end.setText(">");
					PartitionSelectEvent event2 = new PartitionSelectEvent();
					event2.setSource(this.getLocation());
					int newFirstPartition = Math.min(_maxPartitions-_maxShowedPartitions,_firstPartition+_maxShowedPartitions);
					event2.setFirstPartitionIndex(newFirstPartition);
					event2.setPartitionSize(_subsetSize);
					event2.setSelectedPartition(newFirstPartition);
					end.addEventModel(event2);
					if (_controlTarget != null)
					{
						end.setTarget(_controlTarget);
					}
					if (_contolEvent != null)
					{
						end.addEventModel(_contolEvent);
					}
					partitionSelection.add(end);
				}
			}
			this.add(partitionSelection);
		}
	}
}