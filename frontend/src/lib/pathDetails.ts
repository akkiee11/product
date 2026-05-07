// Static action plans for each path. Same content shown to every user
// in V1; Claude Haiku will personalize lender names + amounts in V2.

export type Step = {
  key: string; // stable identifier — used as localStorage key for checkbox state
  when?: string; // "Week 1", "Month 2", etc.
  title: string;
  body?: string;
};

export type PhoneScript = {
  title: string;
  intro: string;
  script: string;
};

export type Pushback = {
  ifTheySay: string;
  youSay: string;
};

export type PathDetail = {
  oneLiner: string;
  pitch: string;
  warning?: string;
  steps: Step[];
  scripts?: PhoneScript[];
  docs?: string[];
  pushback?: Pushback[];
};

export const PATH_DETAILS: Record<string, PathDetail> = {
  STATUS_QUO: {
    oneLiner: "What happens if you do nothing.",
    pitch:
      "This is your baseline — what your debt looks like if you keep paying minimums. We include it so you can see exactly what doing nothing costs you.",
    warning:
      "This is not a plan. It's the slow-bleed scenario. Use it as a benchmark for the savings of any other path.",
    steps: [
      {
        key: "auto-pay-minimums",
        title: "Set up auto-pay for every minimum due, today",
        body: "Missing a single minimum drops your CIBIL 30–80 points and triggers ₹500–1,500 late fees. Auto-pay is free and removes the human error.",
      },
      {
        key: "track-interest",
        when: "Monthly",
        title: "Track total interest paid each month",
        body: "Your bank app shows it. Watching ₹15–40k/month evaporate into interest is the single most motivating thing you can do.",
      },
      {
        key: "rerun-quarterly",
        when: "Every 3 months",
        title: "Re-run your Debt MRI",
        body: "Your situation changes — salary hikes, bonus, partner's income, expenses. Re-run and pick a real path the moment a window opens.",
      },
    ],
  },

  SMART_PATH: {
    oneLiner: "Convert toxic CC debt to EMI, then avalanche surplus.",
    pitch:
      "The mathematically best path for most salaried Indians who are current on payments. You convert revolving credit-card debt (~36–48% APR) to a structured EMI (~14–18% APR), then use the freed-up cash to attack your highest-rate active loan.",
    steps: [
      {
        key: "smart-call-cc",
        when: "Week 1",
        title: "Call your highest-interest credit card issuer",
        body: 'Ask for "outstanding to EMI" or "balance to EMI" conversion. Most banks (HDFC, ICICI, SBI, Axis, Kotak) offer this at 14–18% APR over 12–48 months. Use the script below.',
      },
      {
        key: "smart-confirm-emi",
        when: "Week 1",
        title: "Confirm EMI terms in writing (SMS/email)",
        body: "Bank will SMS the EMI amount, tenure, and processing fee. Save it. If processing fee is >2% of outstanding, push back — competitors charge 1–1.5%.",
      },
      {
        key: "smart-autopay",
        when: "Week 1",
        title: "Set up auto-pay for the new EMI",
        body: "Single missed EMI on this conversion = the bank can revert it to revolving rate. Auto-pay makes that impossible.",
      },
      {
        key: "smart-other-cards",
        when: "Week 2",
        title: "Repeat for every other credit card with outstanding > ₹25,000",
        body: "Don't skip cards just because they're smaller. Each one converted is one less revolving wound.",
      },
      {
        key: "smart-pick-target",
        when: "Week 2",
        title: "Pick your avalanche target — the highest-APR active loan after CC conversion",
        body: "Usually it's a Personal Loan from an NBFC (16–22% APR) or your education loan. List your remaining loans by APR; the top one is your target.",
      },
      {
        key: "smart-attack",
        when: "Week 3+, monthly",
        title: "Throw all surplus cash at the avalanche target as part-payment",
        body: "Surplus = take-home – essentials – all EMIs. Even ₹5–10k extra/month against the highest-rate loan saves lakhs over the loan's life.",
      },
      {
        key: "smart-no-new-debt",
        when: "Always",
        title: "Don't take new debt to pay debt",
        body: "No personal loans to clear cards. No new BNPL. No EMI on lifestyle purchases. The fastest way to undo Smart Path is taking new debt.",
      },
    ],
    scripts: [
      {
        title: "Phone script — CC outstanding to EMI",
        intro:
          "Call the customer-care number on the back of your card. Ask for the EMI/loan-on-card desk. Then read this:",
        script:
          'Hi, I want to convert my outstanding balance on card ending [LAST 4 DIGITS] to a structured EMI.\n\nCan you tell me:\n  1. The interest rate you can offer for this conversion?\n  2. The available tenures — 12, 24, 36, or 48 months?\n  3. The processing fee (and is it negotiable)?\n  4. The total interest I\'d pay vs continuing to revolve?\n\nI\'m comparing offers across my cards, so I need the rate in writing before I confirm.',
      },
    ],
    docs: [
      "Recent credit card statement (3 months)",
      "PAN card",
      "Employment proof (salary slip / appointment letter) — sometimes asked for >₹3L conversions",
      "A second card with available limit — leverage point if your bank pushes back",
    ],
    pushback: [
      {
        ifTheySay: "Sir, you don't qualify for EMI conversion.",
        youSay:
          "Please escalate this to your supervisor or grievance officer. RBI guidelines allow EMI conversion on credit card outstanding for any current account holder. I'd like the denial in writing with a reference number.",
      },
      {
        ifTheySay: "We can offer 24% APR for 12 months.",
        youSay:
          "That's higher than what [HDFC / ICICI / Axis] is offering at 16% for 36 months. I'll convert with whichever bank gives me the best rate. Can you match 16% over 36, or should I move my balance there?",
      },
      {
        ifTheySay: "Processing fee is 3% of outstanding.",
        youSay:
          "Industry standard is 1–2%. I'd like a fee waiver or a reduction to 1.5%. If not, I'll go to my other card issuer.",
      },
    ],
  },

  AGGRESSIVE_SMART_PATH: {
    oneLiner: "Smart Path + ₹20K/month income boost. Cuts time-to-debt-free in half.",
    pitch:
      "Same plan as Smart Path, but you add ₹15–25k/month of side income (freelance, side gig, job switch, weekend work). Every rupee of that side income goes to the avalanche target.",
    steps: [
      {
        key: "agg-do-smart",
        title: "Do every step of Smart Path first",
        body: "CC-to-EMI conversion, autopay, avalanche pick — all of that. Income boost is on top, not instead.",
      },
      {
        key: "agg-list-skills",
        when: "Week 1",
        title: "List 3 monetisable skills you already have",
        body: "Be honest. What can you bill ₹500–2,500/hour for, today? Examples: technical writing, Excel/data entry, English tutoring, design, video editing, code reviews, accounting.",
      },
      {
        key: "agg-pick-channel",
        when: "Week 2",
        title: "Pick one income channel and start onboarding",
        body: "Salaried? Try Internshala / Upwork / Topmate / Cluvio for side gigs. Have 3+ years experience? Negotiate a job switch — typical Indian switch nets +30% salary, that's an instant ₹15–25k/mo.",
      },
      {
        key: "agg-first-rupee",
        when: "Month 2",
        title: "Earn your first ₹5,000 side income",
        body: "The first rupee is the hardest. Once it lands, the rest scales. Don't aim for ₹20k month 1 — aim for ₹5k.",
      },
      {
        key: "agg-redirect-100",
        when: "Month 2+",
        title: "Send 100% of side income to the avalanche target",
        body: "Don't lifestyle-creep this money. Treat it as not-yours. Direct transfer the day it lands — to a separate savings account if needed, then sweep monthly to debt.",
      },
      {
        key: "agg-reassess",
        when: "Month 6",
        title: "Reassess: sustain, scale, or stop?",
        body: "If you're hitting ₹20k/mo without burning out — keep it up. If you're burning out — drop to ₹10k. If income is now ₹50k/mo — start an emergency fund alongside debt payoff.",
      },
    ],
  },

  DIY_SETTLEMENT: {
    oneLiner: "Negotiate directly with banks. ~60% payout. CIBIL hit, tax exposure.",
    pitch:
      "You stop paying, let accounts go delinquent for 60–120 days, and negotiate a one-time settlement directly with the bank — no Freed, no middleman. Cheaper than full settlement, but real costs.",
    warning:
      "Only consider this if Smart Path is genuinely impossible — you've lost income, can't make minimums even after EMI conversion. CIBIL drops to ~580 for 2–3 years. Waived debt over ₹50k counts as taxable income (Section 56(2)(x)).",
    steps: [
      {
        key: "diy-fund",
        when: "Months 1–3",
        title: "Build a settlement fund worth 50–60% of total CC outstanding",
        body: "You need cash ready when the bank agrees. Without it, you can't close the deal and damage your CIBIL for nothing.",
      },
      {
        key: "diy-stop-revolving",
        when: "Day 1",
        title: "Stop using the cards immediately. Don't take cash advances.",
        body: "Every new transaction increases what you'll need to settle and weakens your negotiation position.",
      },
      {
        key: "diy-go-delinquent",
        when: "Months 1–4",
        title: "Wait through the collection escalation",
        body: "Days 30–60: Bank calls. Stay calm. Don't promise full payment. Days 60–120: Recovery agents call. Same rule.",
      },
      {
        key: "diy-call-settlement",
        when: "Month 4",
        title: "Call the bank's settlement department directly",
        body: 'Ask for "settlement department" or "loss recovery". Use the script below. Open at 50%, settle at 60–70%.',
      },
      {
        key: "diy-letter-first",
        when: "Negotiation",
        title: "Get the settlement letter BEFORE paying anything",
        body: "Letter must say \"Full and Final Settlement\". \"Part payment\" or \"Without prejudice\" wording lets them re-pursue you later.",
      },
      {
        key: "diy-pay-trace",
        when: "Settlement day",
        title: "Pay only via NEFT or DD. Never cash. Get a receipt.",
        body: "Keep the bank statement, receipt, settlement letter, and final CIBIL update notice in one folder. You'll need them in 2–3 years to dispute any incorrect status.",
      },
      {
        key: "diy-tax-bomb",
        when: "Next ITR filing",
        title: "Plan for the tax bomb",
        body: "Waived debt > ₹50,000/year is Income from Other Sources at your slab rate. On a ₹2L waiver in the 30% slab, that's ₹60k extra tax. Get a CA before signing.",
      },
      {
        key: "diy-cibil-recovery",
        when: "Years 1–3 post-settlement",
        title: "Rebuild CIBIL: secured card + clean repayment",
        body: "Take a secured credit card against an FD (₹15–25k FD). Use it for 1 grocery bill/month. Pay on time. Score recovers to ~700 in 24–36 months.",
      },
    ],
    scripts: [
      {
        title: "Phone script — settlement negotiation",
        intro:
          'After 60–90 days of delinquency, when you call the bank, ask for "settlement department" or "loss recovery". Then:',
        script:
          'Hi, I\'m calling about my account [account number]. My financial situation has changed and I cannot make full payment. I want to settle this account in full as a one-time payment.\n\nI can pay [50%] of the outstanding right now via NEFT, in full and final settlement.\n\n[They will counter at 75–80%. Hold at 60–70%.]\n\nI need this in writing as a "full and final settlement" letter, before I transfer the money. The letter should reference the settled amount and confirm no further dues.\n\n[If they refuse]: I understand. Please escalate this to your settlement manager. I\'ll call back tomorrow.',
      },
    ],
    docs: [
      "Bank statements showing income drop / financial hardship",
      "Termination letter / medical bills / any documented hardship",
      "Cash ready in your account (60% of outstanding)",
      "PAN — for the tax declaration on waived debt",
    ],
  },

  FULL_SETTLEMENT: {
    oneLiner: "Freed-style. Stop paying everything. Pay back ~30–50% lump sum. CIBIL nuked.",
    pitch:
      "What companies like Freed sell. You stop paying creditors entirely, save into their program, they negotiate lump-sum settlements at 30–50%. Lowest cash cost. Highest hidden cost.",
    warning:
      "We include this for transparency, not endorsement. Read all 9 risks before considering. CIBIL drops to ~400 for 5–7 years. Tax bomb on waived debt could be ₹3–5L on a ₹20L settlement. Recovery agents may harass you. Section 138 / civil suits are possible. No new credit, home loan, rental approvals until ~2032–2033.",
    steps: [
      {
        key: "full-stop-paying",
        when: "Day 1",
        title: "Stop all payments to creditors",
        body: "Once you start, you cannot un-start. Your credit life as you know it ends here.",
      },
      {
        key: "full-program-fund",
        when: "Months 1–9",
        title: "Save into the settlement program account, not the bank's",
        body: "If using Freed/SingleDebt, money goes to their escrow. If DIY, save into your own dedicated account. Build the lump sum first.",
      },
      {
        key: "full-collections",
        when: "Months 1–9",
        title: "Endure 6–9 months of collection calls and recovery agent visits",
        body: "Document everything. Recording calls is legal in India for self-protection. RBI prohibits agents calling before 7am or after 7pm. File complaints with the bank's grievance officer if violated.",
      },
      {
        key: "full-lump-settle",
        when: "Month 9–12",
        title: "Lump-sum settle at 30–50% of outstanding",
        body: "Get a written 'full and final settlement' letter. Pay via NEFT only. Keep all paperwork forever — CIBIL disputes can come up years later.",
      },
      {
        key: "full-program-fees",
        when: "Through the process",
        title: "Pay program fees (Freed, SingleDebt, etc. charge ~25% of debt savings)",
        body: "If you save ₹10L vs full repayment, the program keeps ~₹2.5L. This is not in their initial pitch.",
      },
      {
        key: "full-tax-filing",
        when: "Next ITR filing",
        title: "Declare every waived rupee as taxable income",
        body: "Bank will issue Form 26AS / AIS showing waived amount. Your CA needs to declare this under Section 56(2)(x). On a ₹15L waiver in the 30% slab, that's ₹4.5L extra tax owed.",
      },
      {
        key: "full-section-138",
        when: "Risk window",
        title: "Be aware of Section 138 and civil suit risk",
        body: "If any cheque (post-dated, EMI auto-debit) bounced during the delinquency window, banks can file Section 138 case. Civil suits for amounts > ₹5L are common. Get a lawyer if served.",
      },
      {
        key: "full-cibil-frozen",
        when: "Years 1–7 post-settlement",
        title: "No new credit, home loan, rental approval until ~2032–2033",
        body: 'Your CIBIL will read "settled" for 7 years. Most lenders auto-reject "settled" status. Some rentals ask for CIBIL too. Plan your life around this.',
      },
      {
        key: "full-rebuild",
        when: "Year 7+",
        title: "Once the settled status drops off, rebuild from scratch",
        body: "Secured card → small personal loan repaid on time → repeat. ~5–7 years to a 750+ score from a 400 nadir.",
      },
    ],
    pushback: [
      {
        ifTheySay: "Recovery agent: 'We'll come to your office / parents' house / file police complaint.'",
        youSay:
          "RBI Master Direction on Recovery Agents prohibits intimidation, threats, contact with employer/family, or false claims of police action. I'm recording this call. I'll file a complaint with the bank's grievance officer and the Banking Ombudsman.",
      },
    ],
  },
};
