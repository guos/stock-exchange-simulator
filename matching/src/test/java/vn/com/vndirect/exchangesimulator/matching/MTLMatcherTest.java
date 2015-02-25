package vn.com.vndirect.exchangesimulator.matching;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import vn.com.vndirect.exchangesimulator.matching.index.OrderPriceIndex;
import vn.com.vndirect.exchangesimulator.model.ExecutionReport;
import vn.com.vndirect.exchangesimulator.model.NewOrderSingle;

public class MTLMatcherTest {
	
	private ContinuousSessionMatcher sm;
	
	@Before
	public void setup() {
		sm = new ContinuousSessionMatcher("VND", new PriceRange(10000, 16300, 100), new OrderMatcher(new ContinuousReport()), new OrderPriceIndex());
	}

	@Test
	public void testGivenMTLBuyWithNoLOSell() {
		NewOrderSingle mtlOrder = OrderFactory.createMTLBuy("VND", 3000);
		sm.push(mtlOrder);
		List<ExecutionReport> reports = sm.getLastMatches();
		Assert.assertEquals(0, reports.size());
	}
	
	@Test
	public void testGivenMTLSellWithOneLOBuyWhenQuantityIsMatched() {
		NewOrderSingle order = OrderFactory.createLOBuy("VND", 1000, 13000);
		sm.push(order);
		NewOrderSingle mtlOrder = OrderFactory.createMTLSell("VND", 1000);
		sm.push(mtlOrder);
		List<ExecutionReport> reports = sm.getLastMatches();
		Assert.assertEquals(2, reports.size());
		for(ExecutionReport report : reports) {
			verifyReport(report, '2', 1000, 13000);
		}
	}
	
	@Test
	public void testGivenMTLSellWithOneLOBuyWhenQuantityOfMTLOrderIsSmaller() {
		NewOrderSingle order = OrderFactory.createLOBuy("VND", 1000, 13000);
		sm.push(order);
		NewOrderSingle mtlOrder = OrderFactory.createMTLSell("VND", 500);
		sm.push(mtlOrder);
		List<ExecutionReport> reports = sm.getLastMatches();
		Assert.assertEquals(2, reports.size());
		for(ExecutionReport report : reports) {
			verifyReport(report, '2', 500, 13000);
		}
	}
	
	@Test
	public void testGivenMTLSellWithOneLOBuyWhenQuantityOfMTLOrderIsBigger() {
		NewOrderSingle order = OrderFactory.createLOBuy("VND", 1000, 13000);
		sm.push(order);
		NewOrderSingle mtlOrder = OrderFactory.createMTLSell("VND", 1500);
		sm.push(mtlOrder);
		List<ExecutionReport> reports = sm.getLastMatches();
		Assert.assertEquals(3, reports.size());
		verifyReport(reports.get(0), '2', 1000, 13000);
		verifyReport(reports.get(1), '2', 1000, 13000);
		// TODO check logic of exchange with mtl
		//verifyReport(reports.get(2), 'A', 500, 12000);
		
		verifyMTLOrderAfterMatching(mtlOrder, 500, 13100);
	}
	
	@Test
	public void testGivenMTLBuyWithOneLOSellWhenQuantityOfMTLOrderIsBigger() {
		NewOrderSingle order = OrderFactory.createLOSell("VND", 1000, 13000);
		sm.push(order);
		NewOrderSingle mtlOrder = OrderFactory.createMTLBuy("VND", 1200);
		sm.push(mtlOrder);
		List<ExecutionReport> reports = sm.getLastMatches();
		Assert.assertEquals(3, reports.size());
		verifyReport(reports.get(0), '2', 1000, 13000);
		verifyReport(reports.get(1), '2', 1000, 13000);
		// TODO check logic of exchange with mtl
		//verifyReport(reports.get(1), 'A', 13100, 200);
		
		verifyMTLOrderAfterMatching(mtlOrder, 200, 12900);
	}
	
	@Test
	public void testGivenMTLBuyWithoutLOSell() {
		NewOrderSingle order = OrderFactory.createLOBuy("VND", 1000, 13000);
		sm.push(order);
		NewOrderSingle mtlOrder = OrderFactory.createMTLBuy("VND", 1200);
		sm.push(mtlOrder);
		List<ExecutionReport> reports = sm.getLastMatches();
		Assert.assertEquals(0, reports.size());
		Assert.assertEquals(0, mtlOrder.getOrderQty());
	}
	
	@Test
	public void testGivenMTLSellWithoutLOBuy() {
		NewOrderSingle order = OrderFactory.createLOSell("VND", 1000, 13000);
		sm.push(order);
		NewOrderSingle mtlOrder = OrderFactory.createMTLSell("VND", 1200);
		sm.push(mtlOrder);
		List<ExecutionReport> reports = sm.getLastMatches();
		Assert.assertEquals(0, reports.size());
		Assert.assertEquals(0, mtlOrder.getOrderQty());
	}
	

	@Test
	public void testGivenMTLBuyWithOneLOSellWhenQuantityIsMatched() {
		NewOrderSingle order = OrderFactory.createLOSell("VND", 1000, 13000);
		sm.push(order);
		NewOrderSingle mtlOrder = OrderFactory.createMTLBuy("VND", 1000);
		sm.push(mtlOrder);
		List<ExecutionReport> reports = sm.getLastMatches();
		Assert.assertEquals(2, reports.size());
		for(ExecutionReport report : reports) {
			verifyReport(report, '2', 1000, 13000);
		}
	}
	
	@Test
	public void testGivenMTLSellWith2LOBuyWhenQuantityIsMatched() {
		NewOrderSingle order = OrderFactory.createLOBuy("VND", 1000, 13000);
		sm.push(order);
		NewOrderSingle order2 = OrderFactory.createLOBuy("VND", 2000, 13500);
		sm.push(order2);
		NewOrderSingle mtlOrder = OrderFactory.createMTLSell("VND", 3000);
		sm.push(mtlOrder);
		List<ExecutionReport> reports = sm.getLastMatches();
		Assert.assertEquals(4, reports.size());
		verifyReport(reports.get(0), '2', 2000, 13500);
		verifyReport(reports.get(1), '2', 2000, 13500);
		verifyReport(reports.get(2), '2', 1000, 13000);
		verifyReport(reports.get(3), '2', 1000, 13000);
	}
	
	@Test
	public void testGivenMTLBuyWith2LOSellWhenQuantityIsMatched() {
		NewOrderSingle order = OrderFactory.createLOSell("VND", 1000, 13000);
		sm.push(order);
		NewOrderSingle order2 = OrderFactory.createLOSell("VND", 2000, 13500);
		sm.push(order2);
		NewOrderSingle mtlOrder = OrderFactory.createMTLBuy("VND", 3000);
		sm.push(mtlOrder);
		List<ExecutionReport> reports = sm.getLastMatches();
		Assert.assertEquals(4, reports.size());
		verifyReport(reports.get(0), '2', 1000, 13000);
		verifyReport(reports.get(1), '2', 1000, 13000);
		verifyReport(reports.get(2), '2', 2000, 13500);
		verifyReport(reports.get(3), '2', 2000, 13500);
	}
	
	private void verifyReport(ExecutionReport report, char status, int quantity, double price) {
		Assert.assertEquals(status, report.getOrdStatus());
		Assert.assertEquals(quantity, report.getOrderQty());
		Assert.assertEquals(quantity, report.getLastQty());
		Assert.assertEquals(price, report.getPrice(), 0);
		Assert.assertEquals(price, report.getLastPx(), 0);
	}
	
	private void verifyMTLOrderAfterMatching(NewOrderSingle mtlOrder, int quantity, double price) {
		Assert.assertEquals("Remaining part of MTL Order after matching should become LO", '2', mtlOrder.getOrdType());
		Assert.assertEquals(quantity, mtlOrder.getOrderQty());
		Assert.assertEquals(price, mtlOrder.getPrice(), 0);
	}	
	
}