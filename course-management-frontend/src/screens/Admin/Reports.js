import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Form, ProgressBar, Row } from "react-bootstrap";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";
import "../../styles/ManagerReports.css";

const PRESETS = [
    ["TODAY", "Hôm nay"], ["LAST_7_DAYS", "7 ngày gần nhất"],
    ["LAST_30_DAYS", "30 ngày gần nhất"], ["THIS_MONTH", "Tháng này"],
    ["THIS_QUARTER", "Quý này"], ["THIS_YEAR", "Năm nay"],
    ["LAST_QUARTER", "Quý trước"], ["LAST_YEAR", "Năm trước"],
    ["CUSTOM", "Tùy chỉnh"]
];

const Reports = () => {
    const initialRange = useMemo(() => getPresetRange("THIS_YEAR"), []);
    const [courses, setCourses] = useState([]);
    const [courseId, setCourseId] = useState("");
    const [preset, setPreset] = useState("THIS_YEAR");
    const [fromDate, setFromDate] = useState(initialRange.fromDate);
    const [toDate, setToDate] = useState(initialRange.toDate);
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    useEffect(() => {
        authApis().get(endpoints.adminCourseOptions)
            .then(res => setCourses(Array.isArray(res.data) ? res.data : []))
            .catch(ex => console.error("Load report course options error:", ex));
    }, []);

    useEffect(() => {
        if(!fromDate || !toDate || fromDate > toDate) return;
        const load = async () => {
            try {
                setLoading(true);
                setErr("");
                const params = { fromDate, toDate };
                if(courseId) params.courseId = courseId;
                const res = await authApis().get(endpoints.adminReports, { params });
                setData(res.data || {});
            } catch(ex) {
                console.error("Load admin report error:", ex);
                setErr(ex.response?.data?.message || "Không thể tải báo cáo hệ thống!");
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [courseId, fromDate, toDate]);

    const report = data || {};
    const money = value => `${Number(value || 0).toLocaleString("vi-VN")} VNĐ`;
    const percentage = (value, total) => total > 0 ? Math.round(value * 100 / total) : 0;
    const paymentTotal = Number(report.successfulPayments || 0) + Number(report.pendingPayments || 0) + Number(report.failedPayments || 0);
    const enrollmentTotal = Number(report.activeEnrollments || 0) + Number(report.pendingEnrollments || 0) + Number(report.canceledEnrollments || 0);
    const progressTotal = Number(report.activeEnrollments || 0);

    const selectedCourseName = useMemo(() => {
        if(!courseId) return "Toàn hệ thống";
        const course = courses.find(c => String(c.id ?? c.courseId) === String(courseId));
        return course?.name || "Khóa học đã chọn";
    }, [courses, courseId]);

    const changePreset = value => {
        setPreset(value);
        if(value === "CUSTOM") return;
        const range = getPresetRange(value);
        setFromDate(range.fromDate);
        setToDate(range.toDate);
    };

    const resetFilters = () => {
        const range = getPresetRange("THIS_YEAR");
        setCourseId("");
        setPreset("THIS_YEAR");
        setFromDate(range.fromDate);
        setToDate(range.toDate);
        setErr("");
    };

    const invalidRange = fromDate && toDate && fromDate > toDate;

    return (
        <>
            <div className="cm-report-heading mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Báo cáo hệ thống</h2>
                    <p className="text-muted mb-0">Tổng hợp thanh toán, đăng ký học và tiến độ toàn hệ thống theo khoảng thời gian.</p>
                </div>
            </div>

            <Card className="cm-report-filter-card mb-4">
                <Card.Body>
                    <div className="cm-report-filter-grid">
                        <Form.Group>
                            <Form.Label>Phạm vi</Form.Label>
                            <Form.Select value={courseId} onChange={e => setCourseId(e.target.value)}>
                                <option value="">Toàn hệ thống</option>
                                {courses.map(c => <option key={c.id ?? c.courseId} value={c.id ?? c.courseId}>{c.name}</option>)}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>Khoảng thời gian</Form.Label>
                            <Form.Select value={preset} onChange={e => changePreset(e.target.value)}>
                                {PRESETS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>Từ ngày</Form.Label>
                            <Form.Control type="date" value={fromDate} max={toDate || undefined}
                                onChange={e => { setPreset("CUSTOM"); setFromDate(e.target.value); }} />
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>Đến ngày</Form.Label>
                            <Form.Control type="date" value={toDate} min={fromDate || undefined}
                                onChange={e => { setPreset("CUSTOM"); setToDate(e.target.value); }} />
                        </Form.Group>

                        <Button variant="outline-secondary" className="cm-report-reset" onClick={resetFilters}>Đặt lại</Button>
                    </div>
                    {invalidRange && <div className="text-danger small mt-2">Từ ngày không được sau đến ngày.</div>}
                </Card.Body>
            </Card>

            {err && <Alert variant="danger">{err}</Alert>}
            {loading ? <div className="text-center py-5"><MySpinner /></div> : (
                <>
                    <div className="cm-report-scope mb-4">
                        Phạm vi: <strong>{selectedCourseName}</strong>
                        <span>·</span>
                        Thời gian: <strong>{formatRange(fromDate, toDate)}</strong>
                    </div>

                    <Row className="g-3 mb-4">
                        <Col xl={3} md={6}><Card className="cm-report-kpi h-100"><Card.Body><small>Doanh thu thành công</small><h4>{money(report.successfulRevenue)}</h4><span>{report.successfulPayments || 0} giao dịch thành công</span></Card.Body></Card></Col>
                        <Col xl={3} md={6}><Card className="cm-report-kpi h-100"><Card.Body><small>Tỷ lệ thanh toán thành công</small><h4>{percentage(Number(report.successfulPayments || 0), paymentTotal)}%</h4><span>{paymentTotal} giao dịch trong kỳ</span></Card.Body></Card></Col>
                        <Col xl={3} md={6}><Card className="cm-report-kpi h-100"><Card.Body><small>Enrollment đang hoạt động</small><h4>{report.activeEnrollments || 0}</h4><span>{enrollmentTotal} đăng ký trong kỳ</span></Card.Body></Card></Col>
                        <Col xl={3} md={6}><Card className="cm-report-kpi h-100"><Card.Body><small>Hoàn thành lộ trình</small><h4>{report.completedCount || 0}</h4><span>{percentage(Number(report.completedCount || 0), progressTotal)}% enrollment active</span></Card.Body></Card></Col>
                    </Row>

                    <Card className="cm-report-chart-card mb-4">
                        <Card.Body>
                            <div className="cm-report-chart-head">
                                <div><h5>Doanh thu theo thời gian</h5><p>Chỉ tính giao dịch SUCCESS trong khoảng đã chọn.</p></div>
                                <span>{report.revenueGranularity === "MONTH" ? "Theo tháng" : "Theo ngày"}</span>
                            </div>
                            <RevenueChart points={report.revenueTrend || []} />
                        </Card.Body>
                    </Card>

                    <Row className="g-4">
                        <Col lg={4}><DistributionCard title="Thanh toán" note="Phân bổ trạng thái giao dịch trong kỳ.">
                            <ReportRow label="Thành công" value={report.successfulPayments} total={paymentTotal} variant="success" />
                            <ReportRow label="Chờ xử lý" value={report.pendingPayments} total={paymentTotal} variant="warning" />
                            <ReportRow label="Thất bại" value={report.failedPayments} total={paymentTotal} variant="danger" />
                        </DistributionCard></Col>

                        <Col lg={4}><DistributionCard title="Đăng ký học" note="Tình trạng enrollment được tạo trong kỳ.">
                            <ReportRow label="Đang học" value={report.activeEnrollments} total={enrollmentTotal} variant="primary" />
                            <ReportRow label="Chờ thanh toán" value={report.pendingEnrollments} total={enrollmentTotal} variant="warning" />
                            <ReportRow label="Đã hủy" value={report.canceledEnrollments} total={enrollmentTotal} variant="secondary" />
                        </DistributionCard></Col>

                        <Col lg={4}><DistributionCard title="Tiến độ học tập" note="Trạng thái hiện tại của enrollment ACTIVE trong kỳ.">
                            <ReportRow label="Đang học" value={report.inProgressCount} total={progressTotal} variant="primary" />
                            <ReportRow label="Tạm dừng" value={report.pausedCount} total={progressTotal} variant="warning" />
                            <ReportRow label="Hoàn thành" value={report.completedCount} total={progressTotal} variant="success" />
                            <ReportRow label="Chưa có lộ trình" value={report.noPathCount} total={progressTotal} variant="secondary" />
                        </DistributionCard></Col>
                    </Row>
                </>
            )}
        </>
    );
};

const DistributionCard = ({ title, note, children }) => <Card className="cm-report-distribution h-100"><Card.Body><h5>{title}</h5><p>{note}</p>{children}</Card.Body></Card>;

const ReportRow = ({ label, value = 0, total = 0, variant }) => {
    const percent = total > 0 ? Math.min(100, Math.round(Number(value || 0) * 100 / total)) : 0;
    return <div className="cm-report-row"><div className="d-flex justify-content-between mb-1"><span>{label}</span><strong>{value || 0}</strong></div><ProgressBar now={percent} variant={variant} /><small>{percent}%</small></div>;
};

const RevenueChart = ({ points }) => {
    const values = points.map(item => Number(item.revenue || 0));
    const maxValue = Math.max(...values, 0);
    if(points.length === 0) return <div className="cm-report-chart-empty">Chưa có dữ liệu doanh thu trong khoảng đã chọn.</div>;

    const width = 900, height = 280, left = 76, right = 24, top = 20, bottom = 52;
    const innerW = width - left - right, innerH = height - top - bottom;
    const x = index => points.length === 1 ? left + innerW / 2 : left + index * innerW / (points.length - 1);
    const y = value => top + innerH - (maxValue > 0 ? value / maxValue * innerH : 0);
    const polyline = points.map((item, index) => `${x(index)},${y(Number(item.revenue || 0))}`).join(" ");
    const labelStep = Math.max(1, Math.ceil(points.length / 8));

    return <div className="cm-report-chart-wrap">
        <svg viewBox={`0 0 ${width} ${height}`} role="img" aria-label="Biểu đồ doanh thu theo thời gian">
            {[0, 0.25, 0.5, 0.75, 1].map(rate => {
                const yy = top + innerH - rate * innerH;
                return <g key={rate}><line className="cm-chart-grid" x1={left} y1={yy} x2={width - right} y2={yy} /><text className="cm-chart-y-label" x={left - 12} y={yy + 4} textAnchor="end">{compactMoney(maxValue * rate)}</text></g>;
            })}
            <polyline className="cm-chart-line" fill="none" points={polyline} />
            {points.map((item, index) => <g key={`${item.period}-${index}`}>
                <circle className="cm-chart-point" cx={x(index)} cy={y(Number(item.revenue || 0))} r="4"><title>{item.label}: {Number(item.revenue || 0).toLocaleString("vi-VN")} VNĐ</title></circle>
                {(index % labelStep === 0 || index === points.length - 1) && <text className="cm-chart-x-label" x={x(index)} y={height - 18} textAnchor="middle">{item.label}</text>}
            </g>)}
        </svg>
        {maxValue === 0 && <div className="cm-report-chart-zero">Không có doanh thu SUCCESS trong kỳ.</div>}
    </div>;
};

const getPresetRange = preset => {
    const today = startOfDay(new Date());
    let from = new Date(today), to = new Date(today);
    const year = today.getFullYear(), month = today.getMonth(), quarterStart = Math.floor(month / 3) * 3;

    if(preset === "LAST_7_DAYS") from.setDate(today.getDate() - 6);
    if(preset === "LAST_30_DAYS") from.setDate(today.getDate() - 29);
    if(preset === "THIS_MONTH") from = new Date(year, month, 1);
    if(preset === "THIS_QUARTER") from = new Date(year, quarterStart, 1);
    if(preset === "THIS_YEAR") from = new Date(year, 0, 1);
    if(preset === "LAST_QUARTER") {
        from = new Date(year, quarterStart - 3, 1);
        to = new Date(year, quarterStart, 0);
    }
    if(preset === "LAST_YEAR") {
        from = new Date(year - 1, 0, 1);
        to = new Date(year - 1, 11, 31);
    }
    return { fromDate: toIsoDate(from), toDate: toIsoDate(to) };
};

const startOfDay = value => new Date(value.getFullYear(), value.getMonth(), value.getDate());
const toIsoDate = value => `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, "0")}-${String(value.getDate()).padStart(2, "0")}`;
const formatRange = (from, to) => `${formatIsoDate(from)} - ${formatIsoDate(to)}`;
const formatIsoDate = value => value ? value.split("-").reverse().join("/") : "-";
const compactMoney = value => {
    const amount = Number(value || 0);
    if(amount >= 1_000_000_000) return `${(amount / 1_000_000_000).toFixed(amount >= 10_000_000_000 ? 0 : 1)}B`;
    if(amount >= 1_000_000) return `${(amount / 1_000_000).toFixed(amount >= 10_000_000 ? 0 : 1)}M`;
    if(amount >= 1_000) return `${Math.round(amount / 1_000)}K`;
    return `${Math.round(amount)}`;
};

export default Reports;
