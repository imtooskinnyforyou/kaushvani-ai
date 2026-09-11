package com.example.ui.components.d3

import com.example.data.model.ArtisanAnalyticsDashboardData

/**
 * Generates an interactive, responsive HTML page powered by D3.js (v7)
 * to visualize monthly artisan sales performance, product category distribution,
 * and pricing trends based on Room database history.
 *
 * Designed specifically for offline-first Android WebView execution with fallback
 * CDN loading, touch gestures, Material 3 theming, and Hindi/English bilingual labels.
 */
object D3ChartsHtmlGenerator {

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun generateHtml(
        data: ArtisanAnalyticsDashboardData,
        isDarkMode: Boolean = false
    ): String {
        val monthlyJson = buildString {
            append("[")
            data.monthlySales.forEachIndexed { i, m ->
                if (i > 0) append(",")
                append("{")
                append("\"monthKey\":\"${escapeJson(m.monthKey)}\",")
                append("\"monthLabel\":\"${escapeJson(m.monthLabel)}\",")
                append("\"hindiMonthLabel\":\"${escapeJson(m.hindiMonthLabel)}\",")
                append("\"unitsSold\":${m.unitsSold},")
                append("\"totalRevenue\":${m.totalRevenue},")
                append("\"averageSellingPrice\":${m.averageSellingPrice},")
                append("\"averageProfitMarginPercent\":${m.averageProfitMarginPercent},")
                append("\"topSellingCategory\":\"${escapeJson(m.topSellingCategory)}\",")
                append("\"topSellingProduct\":\"${escapeJson(m.topSellingProduct)}\"")
                append("}")
            }
            append("]")
        }

        val categoryJson = buildString {
            append("[")
            data.categoryDistribution.forEachIndexed { i, c ->
                if (i > 0) append(",")
                append("{")
                append("\"category\":\"${escapeJson(c.category)}\",")
                append("\"hindiCategory\":\"${escapeJson(c.hindiCategory)}\",")
                append("\"productCount\":${c.productCount},")
                append("\"totalUnitsSold\":${c.totalUnitsSold},")
                append("\"totalRevenue\":${c.totalRevenue},")
                append("\"percentage\":${c.percentage},")
                append("\"colorHex\":\"${escapeJson(c.colorHex)}\"")
                append("}")
            }
            append("]")
        }

        val pricingJson = buildString {
            append("[")
            data.pricingTrends.forEachIndexed { i, p ->
                if (i > 0) append(",")
                append("{")
                append("\"id\":${p.id},")
                append("\"productId\":${p.productId},")
                append("\"productTitle\":\"${escapeJson(p.productTitle)}\",")
                append("\"category\":\"${escapeJson(p.category)}\",")
                append("\"recordedDate\":${p.recordedDate},")
                append("\"dateLabel\":\"${escapeJson(p.dateLabel)}\",")
                append("\"sellingPrice\":${p.sellingPrice},")
                append("\"materialCost\":${p.materialCost},")
                append("\"laborHours\":${p.laborHours},")
                append("\"hourlyWageRate\":${p.hourlyWageRate},")
                append("\"laborCost\":${p.laborCost},")
                append("\"packagingCost\":${p.packagingCost},")
                append("\"fairFloorPrice\":${p.fairFloorPrice},")
                append("\"suggestedRetailPrice\":${p.suggestedRetailPrice},")
                append("\"profitMarginPercent\":${p.profitMarginPercent},")
                append("\"salePeriod\":\"${escapeJson(p.salePeriod)}\",")
                append("\"salesChannel\":\"${escapeJson(p.salesChannel)}\",")
                append("\"isAboveLivingWage\":${p.isAboveLivingWage}")
                append("}")
            }
            append("]")
        }

        val categoriesJson = buildString {
            append("[\"All\"")
            data.availableCategories.forEach { cat ->
                append(",\"${escapeJson(cat)}\"")
            }
            append("]")
        }

        val bgColor = if (isDarkMode) "#121212" else "#FAF7F2"
        val cardBg = if (isDarkMode) "#1E1E1E" else "#FFFFFF"
        val textColor = if (isDarkMode) "#F5F5F5" else "#2D2622"
        val textSecondary = if (isDarkMode) "#A0A0A0" else "#796F66"
        val borderColor = if (isDarkMode) "#333333" else "#E8E2D9"
        val gridColor = if (isDarkMode) "#2C2C2C" else "#EFEAE1"

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Artisan Sales & Pricing Analytics (D3)</title>
    <!-- Offline first: local asset bundled in Android assets directory -->
    <script src="file:///android_asset/d3.v7.min.js"></script>
    <script>
        // Fallback CDN if asset loading fails
        if (typeof d3 === 'undefined') {
            document.write('<script src="https://cdn.jsdelivr.net/npm/d3@7/dist/d3.min.js"><\/script>');
        }
    </script>
    <style>
        * {
            box-sizing: border-box;
            -webkit-tap-highlight-color: transparent;
            margin: 0;
            padding: 0;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Noto Sans Devanagari", sans-serif;
            background-color: $bgColor;
            color: $textColor;
            padding: 12px 10px 24px 10px;
            overflow-x: hidden;
            user-select: none;
        }
        .chart-card {
            background-color: $cardBg;
            border: 1px solid $borderColor;
            border-radius: 16px;
            padding: 16px 14px;
            margin-bottom: 16px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.04);
        }
        .chart-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 12px;
        }
        .chart-title {
            font-size: 15px;
            font-weight: 700;
            color: $textColor;
        }
        .chart-subtitle {
            font-size: 11px;
            color: $textSecondary;
            margin-top: 2px;
        }
        .toggle-group {
            display: flex;
            background: ${if (isDarkMode) "#2A2A2A" else "#F0ECE1"};
            border-radius: 8px;
            padding: 2px;
        }
        .toggle-btn {
            border: none;
            background: transparent;
            font-size: 10px;
            font-weight: 600;
            padding: 4px 8px;
            border-radius: 6px;
            color: $textSecondary;
            cursor: pointer;
            transition: all 0.2s;
        }
        .toggle-btn.active {
            background: #C85A32;
            color: #FFFFFF;
            box-shadow: 0 1px 3px rgba(0,0,0,0.15);
        }
        .svg-container {
            width: 100%;
            overflow: visible;
            position: relative;
        }
        svg {
            display: block;
            width: 100%;
            height: auto;
            overflow: visible;
        }
        .tooltip {
            position: absolute;
            opacity: 0;
            background: rgba(33, 27, 24, 0.95);
            color: #FFFFFF;
            padding: 8px 10px;
            border-radius: 8px;
            font-size: 11px;
            pointer-events: none;
            transition: opacity 0.15s ease-out;
            box-shadow: 0 4px 12px rgba(0,0,0,0.25);
            z-index: 100;
            max-width: 220px;
            line-height: 1.4;
        }
        .legend-container {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-top: 12px;
            justify-content: center;
        }
        .legend-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 11px;
            color: $textColor;
            background: ${if (isDarkMode) "#262626" else "#F7F4EE"};
            border: 1px solid $borderColor;
            padding: 4px 8px;
            border-radius: 20px;
            cursor: pointer;
            transition: transform 0.15s;
        }
        .legend-item:active {
            transform: scale(0.96);
        }
        .legend-color {
            width: 10px;
            height: 10px;
            border-radius: 50%;
        }
        .filter-pills {
            display: flex;
            overflow-x: auto;
            gap: 6px;
            padding-bottom: 6px;
            margin-bottom: 8px;
            scrollbar-width: none;
        }
        .filter-pills::-webkit-scrollbar {
            display: none;
        }
        .pill {
            flex-shrink: 0;
            font-size: 11px;
            padding: 4px 10px;
            border-radius: 14px;
            background: ${if (isDarkMode) "#262626" else "#F2ECE3"};
            color: $textSecondary;
            border: 1px solid transparent;
            cursor: pointer;
        }
        .pill.active {
            background: #283593;
            color: #FFFFFF;
            font-weight: 600;
        }
        .axis-label {
            font-size: 10px;
            fill: $textSecondary;
        }
        .grid line {
            stroke: $gridColor;
            stroke-opacity: 0.8;
            shape-rendering: crispEdges;
        }
        .donut-center-title {
            font-size: 11px;
            fill: $textSecondary;
            text-anchor: middle;
            font-weight: 500;
        }
        .donut-center-value {
            font-size: 15px;
            fill: $textColor;
            text-anchor: middle;
            font-weight: 700;
        }
    </style>
</head>
<body>

    <!-- 1. Monthly Artisan Sales Performance Chart -->
    <div class="chart-card">
        <div class="chart-header">
            <div>
                <div class="chart-title">मासिक शिल्प बिक्री (Monthly Sales Performance)</div>
                <div class="chart-subtitle">Room डेटाबेस आधारित वास्तविक बिक्री और मांग रुझान</div>
            </div>
            <div class="toggle-group">
                <button id="btnRevenue" class="toggle-btn active" onclick="setSalesMode('revenue')">₹ राजस्व</button>
                <button id="btnUnits" class="toggle-btn" onclick="setSalesMode('units')">इकाइयाँ</button>
            </div>
        </div>
        <div class="svg-container" id="monthlySalesContainer">
            <div id="monthlyTooltip" class="tooltip"></div>
            <svg id="monthlySalesSvg" viewBox="0 0 360 220" preserveAspectRatio="xMidYMid meet"></svg>
        </div>
    </div>

    <!-- 2. Product Category Distribution Donut Chart -->
    <div class="chart-card">
        <div class="chart-header">
            <div>
                <div class="chart-title">शिल्प श्रेणी वितरण (Category Distribution)</div>
                <div class="chart-subtitle">कैटलॉग विविधता व राजस्व भागीदारी का अनुपात</div>
            </div>
            <div class="toggle-group">
                <button id="btnCatRev" class="toggle-btn active" onclick="setCategoryMode('revenue')">₹ मान</button>
                <button id="btnCatCount" class="toggle-btn" onclick="setCategoryMode('count')">शिल्प</button>
            </div>
        </div>
        <div class="svg-container" id="categoryContainer">
            <div id="categoryTooltip" class="tooltip"></div>
            <svg id="categorySvg" viewBox="0 0 360 210" preserveAspectRatio="xMidYMid meet"></svg>
        </div>
        <div class="legend-container" id="categoryLegend"></div>
    </div>

    <!-- 3. Pricing Trends & Fair Wage Living Floor -->
    <div class="chart-card">
        <div class="chart-header">
            <div>
                <div class="chart-title">मूल्य रुझान व उचित पारिश्रमिक (Pricing Trends)</div>
                <div class="chart-subtitle">विक्रय मूल्य बनाम कच्चा माल व शिल्पकार मजदूरी सुरक्षा</div>
            </div>
        </div>
        <div class="filter-pills" id="pricingFilterPills"></div>
        <div class="svg-container" id="pricingTrendsContainer">
            <div id="pricingTooltip" class="tooltip"></div>
            <svg id="pricingTrendsSvg" viewBox="0 0 360 230" preserveAspectRatio="xMidYMid meet"></svg>
        </div>
    </div>

    <script>
        // Data injected from Android Room Database
        const monthlyData = $monthlyJson;
        const categoryData = $categoryJson;
        const pricingData = $pricingJson;
        const availableCategories = $categoriesJson;

        let currentSalesMode = 'revenue';
        let currentCategoryMode = 'revenue';
        let selectedCategoryFilter = 'All';

        // Initialize Charts when DOM & D3 ready
        function initCharts() {
            if (typeof d3 === 'undefined') {
                setTimeout(initCharts, 100);
                return;
            }
            renderMonthlySalesChart();
            renderCategoryDonutChart();
            renderPricingCategoryPills();
            renderPricingTrendsChart();
        }

        // --- 1. Monthly Sales Performance Chart ---
        function setSalesMode(mode) {
            currentSalesMode = mode;
            document.getElementById('btnRevenue').classList.toggle('active', mode === 'revenue');
            document.getElementById('btnUnits').classList.toggle('active', mode === 'units');
            renderMonthlySalesChart();
        }

        function renderMonthlySalesChart() {
            const svg = d3.select("#monthlySalesSvg");
            svg.selectAll("*").remove();

            const width = 360;
            const height = 220;
            const margin = { top: 20, right: 15, bottom: 35, left: 45 };
            const innerWidth = width - margin.left - margin.right;
            const innerHeight = height - margin.top - margin.bottom;

            const g = svg.append("g")
                .attr("transform", `translate(${'$'}{margin.left},${'$'}{margin.top})`);

            const isRev = currentSalesMode === 'revenue';
            const yValue = d => isRev ? d.totalRevenue : d.unitsSold;

            const x = d3.scaleBand()
                .domain(monthlyData.map(d => d.monthLabel))
                .range([0, innerWidth])
                .padding(0.38);

            const maxY = d3.max(monthlyData, yValue) || 100;
            const y = d3.scaleLinear()
                .domain([0, maxY * 1.15])
                .nice()
                .range([innerHeight, 0]);

            // Gridlines
            g.append("g")
                .attr("class", "grid")
                .call(d3.axisLeft(y).ticks(4).tickSize(-innerWidth).tickFormat(""));

            // Gradient for bars
            const defs = svg.append("defs");
            const grad = defs.append("linearGradient")
                .attr("id", "barGradient")
                .attr("x1", "0%").attr("y1", "0%")
                .attr("x2", "0%").attr("y2", "100%");
            grad.append("stop").attr("offset", "0%").attr("stop-color", isRev ? "#C85A32" : "#283593");
            grad.append("stop").attr("offset", "100%").attr("stop-color", isRev ? "#E28763" : "#5C6BC0");

            // Area curve gradient
            const areaGrad = defs.append("linearGradient")
                .attr("id", "areaGradient")
                .attr("x1", "0%").attr("y1", "0%")
                .attr("x2", "0%").attr("y2", "100%");
            areaGrad.append("stop").attr("offset", "0%").attr("stop-color", isRev ? "#C85A32" : "#283593").attr("stop-opacity", 0.35);
            areaGrad.append("stop").attr("offset", "100%").attr("stop-color", isRev ? "#C85A32" : "#283593").attr("stop-opacity", 0.0);

            // Area curve
            const area = d3.area()
                .curve(d3.curveMonotoneX)
                .x(d => x(d.monthLabel) + x.bandwidth() / 2)
                .y0(innerHeight)
                .y1(d => y(yValue(d)));

            g.append("path")
                .datum(monthlyData)
                .attr("fill", "url(#areaGradient)")
                .attr("d", area);

            // Line curve
            const line = d3.line()
                .curve(d3.curveMonotoneX)
                .x(d => x(d.monthLabel) + x.bandwidth() / 2)
                .y(d => y(yValue(d)));

            g.append("path")
                .datum(monthlyData)
                .attr("fill", "none")
                .attr("stroke", isRev ? "#C85A32" : "#283593")
                .attr("stroke-width", 2)
                .attr("d", line);

            // Bars with rounded top
            const tooltip = d3.select("#monthlyTooltip");

            g.selectAll(".bar")
                .data(monthlyData)
                .enter().append("rect")
                .attr("class", "bar")
                .attr("x", d => x(d.monthLabel))
                .attr("y", d => y(yValue(d)))
                .attr("width", x.bandwidth())
                .attr("height", d => innerHeight - y(yValue(d)))
                .attr("fill", "url(#barGradient)")
                .attr("rx", 5)
                .attr("ry", 5)
                .on("touchstart mouseover", function(event, d) {
                    d3.select(this).attr("opacity", 0.8);
                    tooltip.style("opacity", 1)
                        .html(`
                            <strong>${'$'}{d.hindiMonthLabel} (${'$'}{d.monthLabel})</strong><br/>
                            💰 राजस्व: ₹${'$'}{d.totalRevenue.toLocaleString('en-IN')}<br/>
                            📦 बिक्री: ${'$'}{d.unitsSold} इकाइयाँ<br/>
                            ✨ शीर्ष शिल्प: ${'$'}{d.topSellingProduct}<br/>
                            📈 लाभ मार्जिन: ${'$'}{d.averageProfitMarginPercent}%
                        `)
                        .style("left", Math.min(x(d.monthLabel) + 20, 140) + "px")
                        .style("top", "20px");

                    if (window.AndroidBridge) {
                        window.AndroidBridge.onItemClicked("monthly_sales", d.monthKey, `${'$'}{d.monthLabel}: ₹${'$'}{d.totalRevenue}`);
                    }
                })
                .on("touchend mouseout", function() {
                    d3.select(this).attr("opacity", 1);
                    tooltip.style("opacity", 0);
                });

            // X Axis
            g.append("g")
                .attr("transform", `translate(0,${'$'}{innerHeight})`)
                .call(d3.axisBottom(x).tickSize(0))
                .call(g => g.select(".domain").attr("stroke", "$borderColor"))
                .selectAll("text")
                .attr("class", "axis-label")
                .attr("dy", "10px");

            // Y Axis
            const yAxis = d3.axisLeft(y)
                .ticks(4)
                .tickFormat(d => isRev ? (d >= 1000 ? "₹" + Math.round(d/1000) + "k" : "₹" + d) : d);

            g.append("g")
                .call(yAxis)
                .call(g => g.select(".domain").remove())
                .selectAll("text")
                .attr("class", "axis-label");
        }

        // --- 2. Category Donut Chart ---
        function setCategoryMode(mode) {
            currentCategoryMode = mode;
            document.getElementById('btnCatRev').classList.toggle('active', mode === 'revenue');
            document.getElementById('btnCatCount').classList.toggle('active', mode === 'count');
            renderCategoryDonutChart();
        }

        function renderCategoryDonutChart() {
            const svg = d3.select("#categorySvg");
            svg.selectAll("*").remove();

            const width = 360;
            const height = 210;
            const radius = Math.min(width, height) / 2 - 15;

            const g = svg.append("g")
                .attr("transform", `translate(${'$'}{width / 2},${'$'}{height / 2})`);

            const isRev = currentCategoryMode === 'revenue';
            const valueFn = d => isRev ? d.totalRevenue : d.productCount;

            const pie = d3.pie()
                .value(valueFn)
                .sort(null)
                .padAngle(0.03);

            const arc = d3.arc()
                .innerRadius(radius * 0.55)
                .outerRadius(radius * 0.90)
                .cornerRadius(4);

            const arcHover = d3.arc()
                .innerRadius(radius * 0.52)
                .outerRadius(radius * 0.96)
                .cornerRadius(5);

            const tooltip = d3.select("#categoryTooltip");

            // Total Center Display
            const totalValue = d3.sum(categoryData, valueFn);
            const centerTitle = g.append("text")
                .attr("class", "donut-center-title")
                .attr("y", -6)
                .text(isRev ? "कुल बिक्री मान" : "कुल शिल्प उत्पाद");

            const centerVal = g.append("text")
                .attr("class", "donut-center-value")
                .attr("y", 16)
                .text(isRev ? "₹" + (totalValue >= 100000 ? (totalValue/100000).toFixed(1) + " L" : Math.round(totalValue/1000) + "k") : totalValue);

            const arcs = g.selectAll(".arc")
                .data(pie(categoryData))
                .enter().append("g")
                .attr("class", "arc");

            arcs.append("path")
                .attr("d", arc)
                .attr("fill", d => d.data.colorHex)
                .attr("stroke", "$cardBg")
                .attr("stroke-width", 2)
                .style("cursor", "pointer")
                .on("touchstart mouseover", function(event, d) {
                    d3.select(this).transition().duration(150).attr("d", arcHover);
                    centerTitle.text(d.data.hindiCategory.split(" ")[0]);
                    centerVal.text(isRev ? "₹" + Math.round(d.data.totalRevenue).toLocaleString('en-IN') : `${'$'}{d.data.productCount} शिल्प`);

                    tooltip.style("opacity", 1)
                        .html(`
                            <strong>${'$'}{d.data.hindiCategory}</strong><br/>
                            💰 राजस्व: ₹${'$'}{d.data.totalRevenue.toLocaleString('en-IN')} (${'$'}{d.data.percentage}%)<br/>
                            📦 उत्पाद संख्या: ${'$'}{d.data.productCount} शिल्प<br/>
                            🏷️ इकाइयाँ बिकीं: ${'$'}{d.data.totalUnitsSold}
                        `)
                        .style("left", "70px")
                        .style("top", "15px");

                    if (window.AndroidBridge) {
                        window.AndroidBridge.onItemClicked("category", d.data.category, `${'$'}{d.data.hindiCategory}: ${'$'}{d.data.percentage}%`);
                    }
                })
                .on("touchend mouseout", function() {
                    d3.select(this).transition().duration(150).attr("d", arc);
                    centerTitle.text(isRev ? "कुल बिक्री मान" : "कुल शिल्प उत्पाद");
                    centerVal.text(isRev ? "₹" + (totalValue >= 100000 ? (totalValue/100000).toFixed(1) + " L" : Math.round(totalValue/1000) + "k") : totalValue);
                    tooltip.style("opacity", 0);
                });

            // Update Legend
            const legend = d3.select("#categoryLegend");
            legend.selectAll("*").remove();

            categoryData.forEach(c => {
                const item = legend.append("div")
                    .attr("class", "legend-item")
                    .on("click", () => {
                        selectedCategoryFilter = c.category;
                        renderPricingCategoryPills();
                        renderPricingTrendsChart();
                    });

                item.append("div")
                    .attr("class", "legend-color")
                    .style("background-color", c.colorHex);

                item.append("span")
                    .text(`${'$'}{c.hindiCategory.split(" ")[0]}: ${'$'}{c.percentage}%`);
            });
        }

        // --- 3. Pricing Trends & Living Wage Chart ---
        function renderPricingCategoryPills() {
            const container = d3.select("#pricingFilterPills");
            container.selectAll("*").remove();

            availableCategories.forEach(cat => {
                container.append("button")
                    .attr("class", "pill" + (cat === selectedCategoryFilter ? " active" : ""))
                    .text(cat === "All" ? "समस्त (All)" : cat)
                    .on("click", () => {
                        selectedCategoryFilter = cat;
                        renderPricingCategoryPills();
                        renderPricingTrendsChart();
                    });
            });
        }

        function renderPricingTrendsChart() {
            const svg = d3.select("#pricingTrendsSvg");
            svg.selectAll("*").remove();

            const filteredData = selectedCategoryFilter === "All"
                ? pricingData
                : pricingData.filter(d => d.category === selectedCategoryFilter);

            const displayData = filteredData.length > 0 ? filteredData : pricingData;

            const width = 360;
            const height = 230;
            const margin = { top: 20, right: 15, bottom: 40, left: 45 };
            const innerWidth = width - margin.left - margin.right;
            const innerHeight = height - margin.top - margin.bottom;

            const g = svg.append("g")
                .attr("transform", `translate(${'$'}{margin.left},${'$'}{margin.top})`);

            const x = d3.scalePoint()
                .domain(displayData.map((d, i) => `${'$'}{d.dateLabel}_${'$'}{i}`))
                .range([0, innerWidth])
                .padding(0.2);

            const maxPrice = d3.max(displayData, d => Math.max(d.sellingPrice, d.fairFloorPrice, d.suggestedRetailPrice)) || 5000;
            const y = d3.scaleLinear()
                .domain([0, maxPrice * 1.18])
                .nice()
                .range([innerHeight, 0]);

            // Gridlines
            g.append("g")
                .attr("class", "grid")
                .call(d3.axisLeft(y).ticks(4).tickSize(-innerWidth).tickFormat(""));

            // Shaded Living Wage Floor Zone (0 to fairFloorPrice)
            const livingWageArea = d3.area()
                .curve(d3.curveMonotoneX)
                .x((d, i) => x(`${'$'}{d.dateLabel}_${'$'}{i}`))
                .y0(innerHeight)
                .y1(d => y(d.fairFloorPrice));

            const defs = svg.append("defs");
            const fairWageGrad = defs.append("linearGradient")
                .attr("id", "fairWageGradient")
                .attr("x1", "0%").attr("y1", "0%")
                .attr("x2", "0%").attr("y2", "100%");
            fairWageGrad.append("stop").attr("offset", "0%").attr("stop-color", "#2E7D32").attr("stop-opacity", 0.18);
            fairWageGrad.append("stop").attr("offset", "100%").attr("stop-color", "#2E7D32").attr("stop-opacity", 0.03);

            g.append("path")
                .datum(displayData)
                .attr("fill", "url(#fairWageGradient)")
                .attr("d", livingWageArea);

            // Line 1: Fair Floor Price (Dashed Green)
            const floorLine = d3.line()
                .curve(d3.curveMonotoneX)
                .x((d, i) => x(`${'$'}{d.dateLabel}_${'$'}{i}`))
                .y(d => y(d.fairFloorPrice));

            g.append("path")
                .datum(displayData)
                .attr("fill", "none")
                .attr("stroke", "#2E7D32")
                .attr("stroke-width", 1.8)
                .attr("stroke-dasharray", "4,4")
                .attr("d", floorLine);

            // Line 2: Material Cost (Dotted Amber)
            const matLine = d3.line()
                .curve(d3.curveMonotoneX)
                .x((d, i) => x(`${'$'}{d.dateLabel}_${'$'}{i}`))
                .y(d => y(d.materialCost));

            g.append("path")
                .datum(displayData)
                .attr("fill", "none")
                .attr("stroke", "#D97706")
                .attr("stroke-width", 1.5)
                .attr("stroke-dasharray", "2,3")
                .attr("d", matLine);

            // Line 3: Actual Realized Selling Price (Solid Vibrant Terracotta)
            const sellLine = d3.line()
                .curve(d3.curveMonotoneX)
                .x((d, i) => x(`${'$'}{d.dateLabel}_${'$'}{i}`))
                .y(d => y(d.sellingPrice));

            g.append("path")
                .datum(displayData)
                .attr("fill", "none")
                .attr("stroke", "#C85A32")
                .attr("stroke-width", 2.6)
                .attr("d", sellLine);

            // Points on Selling Price
            const tooltip = d3.select("#pricingTooltip");

            g.selectAll(".price-point")
                .data(displayData)
                .enter().append("circle")
                .attr("class", "price-point")
                .attr("cx", (d, i) => x(`${'$'}{d.dateLabel}_${'$'}{i}`))
                .attr("cy", d => y(d.sellingPrice))
                .attr("r", 5)
                .attr("fill", d => d.isAboveLivingWage ? "#C85A32" : "#DC2626")
                .attr("stroke", "#FFFFFF")
                .attr("stroke-width", 2)
                .style("cursor", "pointer")
                .on("touchstart mouseover", function(event, d) {
                    d3.select(this).attr("r", 7.5);
                    tooltip.style("opacity", 1)
                        .html(`
                            <strong>${'$'}{d.productTitle}</strong><br/>
                            📅 तिथि: ${'$'}{d.dateLabel} (${'$'}{d.salePeriod})<br/>
                            🏷️ विक्रय मूल्य: ₹${'$'}{d.sellingPrice.toLocaleString('en-IN')}<br/>
                            🛡️ उचित न्यूनतम मजदूरी: ₹${'$'}{Math.round(d.fairFloorPrice).toLocaleString('en-IN')}<br/>
                            🧵 कच्चा माल: ₹${'$'}{d.materialCost} | श्रम: ${'$'}{d.laborHours} घंटे<br/>
                            ${'$'}{d.isAboveLivingWage ? "✅ उचित आजीविका सुरक्षित" : "⚠️ न्यूनतम मजदूरी से कम"}
                        `)
                        .style("left", Math.max(10, Math.min(x(`${'$'}{d.dateLabel}_${'$'}{displayData.indexOf(d)}`) - 40, 160)) + "px")
                        .style("top", "20px");

                    if (window.AndroidBridge) {
                        window.AndroidBridge.onItemClicked("pricing", d.id.toString(), `${'$'}{d.productTitle}: ₹${'$'}{d.sellingPrice}`);
                    }
                })
                .on("touchend mouseout", function() {
                    d3.select(this).attr("r", 5);
                    tooltip.style("opacity", 0);
                });

            // X Axis
            g.append("g")
                .attr("transform", `translate(0,${'$'}{innerHeight})`)
                .call(d3.axisBottom(x).tickFormat(d => d.split("_")[0]))
                .call(g => g.select(".domain").attr("stroke", "$borderColor"))
                .selectAll("text")
                .attr("class", "axis-label")
                .attr("dy", "10px");

            // Y Axis
            g.append("g")
                .call(d3.axisLeft(y).ticks(4).tickFormat(d => d >= 1000 ? "₹" + Math.round(d/1000) + "k" : "₹" + d))
                .call(g => g.select(".domain").remove())
                .selectAll("text")
                .attr("class", "axis-label");

            // Legend on Bottom of Graph
            const legendG = g.append("g")
                .attr("transform", `translate(5, ${'$'}{innerHeight + 25})`);

            // Selling Price Legend
            legendG.append("line").attr("x1", 0).attr("y1", 5).attr("x2", 15).attr("y2", 5).attr("stroke", "#C85A32").attr("stroke-width", 2);
            legendG.append("circle").attr("cx", 7.5).attr("cy", 5).attr("r", 3).attr("fill", "#C85A32");
            legendG.append("text").attr("x", 20).attr("y", 8).attr("font-size", "9px").attr("fill", "$textColor").text("विक्रय मूल्य");

            // Floor Price Legend
            legendG.append("line").attr("x1", 95).attr("y1", 5).attr("x2", 110).attr("y2", 5).attr("stroke", "#2E7D32").attr("stroke-width", 1.8).attr("stroke-dasharray", "3,3");
            legendG.append("text").attr("x", 115).attr("y", 8).attr("font-size", "9px").attr("fill", "$textColor").text("उचित मजदूरी फ़्लोर");

            // Material Cost Legend
            legendG.append("line").attr("x1", 205).attr("y1", 5).attr("x2", 220).attr("y2", 5).attr("stroke", "#D97706").attr("stroke-width", 1.5).attr("stroke-dasharray", "2,2");
            legendG.append("text").attr("x", 225).attr("y", 8).attr("font-size", "9px").attr("fill", "$textColor").text("कच्चा माल");
        }

        // Initialize on load
        window.addEventListener('DOMContentLoaded', initCharts);
    </script>
</body>
</html>
        """.trimIndent()
    }
}
