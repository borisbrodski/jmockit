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
   table.style.display = table.style.display == 'none' ? 'block' : 'none';
}

function showHideLines(row)
{
   var cellWithLines = row.cells[2].style;
   cellWithLines.display = cellWithLines.display == 'block' ? 'none' : 'block';
}

var metricCol;
function rowOrder(r1, r2)
{
  var t1 = r1.cells[metricCol].title;
  var t2 = r2.cells[metricCol].title;

  if (t1 && t2) {
    var c1 = t1.split('/')[1];
    var c2 = t2.split('/')[1];
    return c1 - c2;
  }

  return t1 ? 1 : -1;
}

function sortRows(tbl, metric)
{
  var colCount = tbl.rows[0].cells.length;
  var startIndex = colCount == 5 ? 1 : 0;
  var rs = new Array();

  for (var i = startIndex; i < tbl.rows.length; i++) {
    rs[i - startIndex] = tbl.rows[i];
  }

  metricCol = colCount - 4 + metric;
  rs.sort(rowOrder);

  for (var i = 0; i < rs.length; i++) {
    rs[i] = rs[i].innerHTML;
  }

  for (var i = 0; i < rs.length; i++) {
    tbl.rows[startIndex + i].innerHTML = rs[i];
  }
}

function sortTables(metric)
{
   var tables = document.getElementsByTagName("table");

   for (var i = 0; i < tables.length; i++) {
      sortRows(tables[i], metric);
   }
}