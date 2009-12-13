var cellShown;
var lineSegmentIdsShown;

function hidePath(cell)
{
   if (lineSegmentIdsShown) {
      setOutlines('none');
      cellShown.style.outlineWidth = 'thin';
      lineSegmentIdsShown = null;

      var sameCell = cell == cellShown;
      cellShown = null;
      return sameCell;
   }

   return false;
}

function setOutlines(outlineStyle)
{
   for (var i = 0; i < lineSegmentIdsShown.length; i++) {
      var item = document.getElementById(lineSegmentIdsShown[i]);
      if (item) item.style.outline = outlineStyle;
   }
}

function showPath(cell, lineSegmentIdsStr)
{
   if (hidePath(cell)) return;

   lineSegmentIdsShown = lineSegmentIdsStr.split(' ');
   setOutlines('thin dashed #0000FF');
   cell.style.outlineWidth = 'medium';
   cellShown = cell;
}

function showHide(callPoints)
{
   var list = callPoints.nextSibling.nextSibling.style;
   list.display = list.display == 'none' ? 'block' : 'none';
}
