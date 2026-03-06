## Workflow
**Before starting any work:**
    1. After receiving an order, first summarize your planned strategy and approach
    2. Estimate the cost of the work (number of tool calls/requests needed)
3. Alert the user if the estimated cost would consume a significant portion of remaining quota (>10%)
    4. Wait for user confirmation or adjustment before proceeding
    5. Only begin implementation after the user approves the plan

## Git Commit Guidelines
    - First line must be maximum 50 characters
- Use single-line commit messages only (no body/additional lines)
    - Do not capitalize the first letter of the commit message

## Linting and Formatting
    - **ALWAYS** run appropriate linters/formatters automatically before committing without asking for permission
    - When making code changes, run appropriate linters BEFORE committing

## Testing
    - **ALWAYS** run `bin/rspec` instead of `rspec`
    - **ALWAYS** run `bin/cucumber` instead of `cucumber`

## Critical Backend Testing Workflow
    **IMPORTANT:** If you change ANY backend (`.clj` or `.cljc`) file:
    1. **DO NOT** automatically run tests after making changes
    2. **STOP** and inform the user that backend files were changed
    3. **INSTRUCT** the user to restart the backend server
    4. **WAIT** for the user to confirm the server has been restarted before running tests

    The test runner does NOT automatically reload backend code changes. Running tests without restarting the backend will test the OLD code, not your changes.
