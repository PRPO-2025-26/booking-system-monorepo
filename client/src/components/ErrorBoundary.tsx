import React from "react";

type Props = {
  children: React.ReactNode;
};

type State = {
  hasError: boolean;
  message?: string;
};

export class ErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, message: error.message };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    // eslint-disable-next-line no-console
    console.error("UI error boundary caught: ", error, info);
  }

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="card" style={{ color: "#ffb4b4" }}>
          <h3>Something went wrong</h3>
          <p>{this.state.message || "Unknown error"}</p>
          <button className="button" onClick={this.handleReload}>
            Reload page
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
