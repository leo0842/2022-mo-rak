import styled from '@emotion/styled';

const StyledTitle = styled.div(
  ({ theme }) => `
  font-size: 2.8rem;
  color: ${theme.colors.PURPLE_100};
`
);

const StyledHelpIconContainer = styled.div(
  ({ theme }) => `
  display: flex;
  justify-content: center;
  width: 2.8rem;
  height: 2.8rem;
  background: ${theme.colors.GRAY_300};
  border-radius: 100%;
`
);

const StyledHelpIcon = styled.img`
  width: 2rem;
`;

const StyledContent = styled.p`
  font-size: 2.4rem;
`;

const StyledHeader = styled.div`
  display: flex;
  gap: 2rem;
  z-index: 1;
`;

export { StyledTitle, StyledHelpIconContainer, StyledHelpIcon, StyledContent, StyledHeader };
