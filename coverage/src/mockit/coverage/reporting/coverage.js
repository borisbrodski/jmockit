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

function showHide(callPoints, listIndex)
{
   var tableCell = callPoints.parentNode;

   if (listIndex >= 0) {
      tableCell = tableCell.parentNode;
   }
   else {
      listIndex = 0;
   }

   var list = tableCell.getElementsByTagName('ol')[listIndex].style;
   list.display = list.display == 'none' ? 'block' : 'none';
}

var filesShown = true;
function showHideAllFiles(header)
{
   var table = header.parentNode.parentNode;
   filesShown = !filesShown;
   var newDisplay = filesShown ? 'block' : 'none';
   var rows = table.rows;
   rows[0].cells[1].style.display = newDisplay;

   for (var i = 1; i < rows.length; i++) {
      rows[i].cells[1].style.display = newDisplay;
   }
}

function showHideFiles(files)
{
   var table = files.parentNode.cells[1].getElementsByTagName('table')[0];
   var body = table.style;
   var wasHidden = body.display == 'none';
   body.display = wasHidden ? 'block' : 'none';
}
