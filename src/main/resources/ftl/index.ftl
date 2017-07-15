<#escape x as x?html>
<#include "header.ftl">
      <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
          <ul class="nav nav-sidebar">
            <li class="active"><a href="#">Flights</a></li>
            <li><a href="#">Status</a></li>
            <li><a href="#">Configuration</a></li>
          </ul>
        </div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <h1 class="page-header">Dashboard</h1>
          <div>
          	<div id="map" style="height:600px; width:800px;"></div>
          </div>
        </div>
      </div>
	<script src="/js/adsb.js"></script>    
    <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAT2lh-lYB8r8fLWUvcD2H7qDruoBm_Zdg&callback=initMap" async defer></script>  
<#include "footer.ftl">
</#escape>