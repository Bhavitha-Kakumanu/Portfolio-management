import { useEffect, useState, useRef } from 'react';

export default function PriceHistoryChart({ history, selectedSymbol }) {
    const containerRef = useRef(null);
    const [width, setWidth] = useState(500);
    const [hoveredIndex, setHoveredIndex] = useState(null);
    const [tooltipPos, setTooltipPos] = useState({ x: 0, y: 0 });

    useEffect(() => {
        if (!containerRef.current) return;
        const resizeObserver = new ResizeObserver((entries) => {
            for (let entry of entries) {
                setWidth(entry.contentRect.width);
            }
        });
        resizeObserver.observe(containerRef.current);
        return () => resizeObserver.disconnect();
    }, []);

    if (!history || history.length === 0) {
        return <p className="empty-state">No price history is available for this stock.</p>;
    }

    const chartData = [...history].sort((a, b) => new Date(a.date) - new Date(b.date));

    const prices = chartData.map((d) => Number(d.closePrice));
    const minPrice = Math.min(...prices);
    const maxPrice = Math.max(...prices);
    const priceRange = maxPrice - minPrice || 1;

    const yMin = Math.max(0, minPrice - priceRange * 0.1);
    const yMax = maxPrice + priceRange * 0.1;
    const yRange = yMax - yMin;

    const height = 220;
    const paddingLeft = 50;
    const paddingRight = 20;
    const paddingTop = 20;
    const paddingBottom = 30;

    const chartWidth = width - paddingLeft - paddingRight;
    const chartHeight = height - paddingTop - paddingBottom;

    const points = chartData.map((d, index) => {
        const x = paddingLeft + (index / (chartData.length - 1 || 1)) * chartWidth;
        const y = paddingTop + chartHeight - ((Number(d.closePrice) - yMin) / yRange) * chartHeight;
        return { x, y, price: Number(d.closePrice), date: d.date };
    });

    let pathD = '';
    if (points.length > 0) {
        pathD = `M ${points[0].x} ${points[0].y} ` + points.slice(1).map((p) => `L ${p.x} ${p.y}`).join(' ');
    }

    let fillD = '';
    if (points.length > 0) {
        fillD = `${pathD} L ${points[points.length - 1].x} ${paddingTop + chartHeight} L ${points[0].x} ${paddingTop + chartHeight} Z`;
    }

    const firstPrice = prices[0];
    const lastPrice = prices[prices.length - 1];
    const isPositive = lastPrice >= firstPrice;
    const strokeColor = isPositive ? 'var(--green)' : 'var(--red)';
    const gradientId = `chart-gradient-${isPositive ? 'green' : 'red'}-${selectedSymbol}`;

    const handleMouseMove = (e) => {
        if (!containerRef.current || points.length === 0) return;
        const rect = containerRef.current.getBoundingClientRect();
        const mouseX = e.clientX - rect.left;

        let closestIndex = 0;
        let closestDist = Math.abs(points[0].x - mouseX);
        for (let i = 1; i < points.length; i++) {
            const dist = Math.abs(points[i].x - mouseX);
            if (dist < closestDist) {
                closestDist = dist;
                closestIndex = i;
            }
        }

        setHoveredIndex(closestIndex);
        setTooltipPos({
            x: points[closestIndex].x,
            y: points[closestIndex].y - 10,
        });
    };

    const handleMouseLeave = () => {
        setHoveredIndex(null);
    };

    const yTicks = [yMin + yRange * 0.1, yMin + yRange * 0.5, yMin + yRange * 0.9];
    const startPriceStr = firstPrice?.toFixed(2) || '0.00';
    const endPriceStr = lastPrice?.toFixed(2) || '0.00';

    return (
        <div ref={containerRef} className="price-chart-container">
            <p className="sr-only">
                The price changed from ${startPriceStr} to ${endPriceStr} over the selected range.
            </p>
            <svg
                width={width}
                height={height}
                onMouseMove={handleMouseMove}
                onMouseLeave={handleMouseLeave}
                role="img"
                aria-label={`Price history chart for ${selectedSymbol}`}
                style={{ overflow: 'visible', display: 'block' }}
            >
                <defs>
                    <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor={strokeColor} stopOpacity="0.2" />
                        <stop offset="100%" stopColor={strokeColor} stopOpacity="0.0" />
                    </linearGradient>
                </defs>

                {/* Y Gridlines */}
                {yTicks.map((val, idx) => {
                    const y = paddingTop + chartHeight - ((val - yMin) / yRange) * chartHeight;
                    return (
                        <g key={idx}>
                            <line
                                x1={paddingLeft}
                                y1={y}
                                x2={width - paddingRight}
                                y2={y}
                                stroke="var(--border)"
                                strokeDasharray="3,3"
                            />
                            <text
                                x={paddingLeft - 8}
                                y={y + 4}
                                textAnchor="end"
                                fill="var(--text-muted)"
                                style={{ fontSize: '10px', fontFamily: 'inherit' }}
                            >
                                ${val.toFixed(2)}
                            </text>
                        </g>
                    );
                })}

                {/* X Ticks */}
                {points.map((p, idx) => {
                    const isTick = idx === 0 || idx === Math.floor(points.length / 2) || idx === points.length - 1;
                    if (!isTick) return null;
                    return (
                        <text
                            key={idx}
                            x={p.x}
                            y={paddingTop + chartHeight + 18}
                            textAnchor="middle"
                            fill="var(--text-muted)"
                            style={{ fontSize: '10px', fontFamily: 'inherit' }}
                        >
                            {p.date}
                        </text>
                    );
                })}

                {/* Fill Area */}
                {fillD && <path d={fillD} fill={`url(#${gradientId})`} />}

                {/* Line Path */}
                {pathD && (
                    <path
                        d={pathD}
                        fill="none"
                        stroke={strokeColor}
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                )}

                {/* Hover indicators */}
                {hoveredIndex !== null && (
                    <g>
                        <line
                            x1={points[hoveredIndex].x}
                            y1={paddingTop}
                            x2={points[hoveredIndex].x}
                            y2={paddingTop + chartHeight}
                            stroke="var(--border)"
                            strokeWidth="1"
                        />
                        <circle
                            cx={points[hoveredIndex].x}
                            cy={points[hoveredIndex].y}
                            r="5"
                            fill={strokeColor}
                            stroke="#fff"
                            strokeWidth="1.5"
                        />
                    </g>
                )}
            </svg>

            {hoveredIndex !== null && (
                <div
                    className="price-chart-tooltip"
                    style={{
                        left: `${tooltipPos.x}px`,
                        top: `${tooltipPos.y - 45}px`,
                    }}
                >
                    <strong className="price-chart-tooltip-value">${points[hoveredIndex].price.toFixed(2)}</strong>
                    <div className="price-chart-tooltip-date">
                        {points[hoveredIndex].date}
                    </div>
                </div>
            )}
        </div>
    );
}
